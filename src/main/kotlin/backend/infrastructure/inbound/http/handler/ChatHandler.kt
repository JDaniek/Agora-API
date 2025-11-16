package backend.infrastructure.inbound.http.handler

import backend.domain.port.inbound.GetChatMessagesQuery
import backend.domain.port.inbound.SendMessageUseCase
import backend.domain.port.outbound.ChatRepository
import backend.infrastructure.inbound.http.dto.chat.WsMessageIn
import backend.infrastructure.inbound.http.dto.chat.WsMessageOut
import backend.infrastructure.inbound.http.mapper.classes.toWsMessageOut
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.json.Json

/**
 * Maneja toda la lógica de negocio para las rutas de Chat (REST y WebSocket).
 * Recibe los casos de uso por inyección de dependencias (Koin).
 */
class ChatHandler(
    private val listMessagesQuery: GetChatMessagesQuery,
    private val sendMessageUseCase: SendMessageUseCase,
    private val chatRepository: ChatRepository // Para validación de membresía
) {

    /**
     * Maneja la petición REST [GET] /chat/{id}/messages
     */
    suspend fun handleGetMessages(call: ApplicationCall) {
        val chatId = call.parameters["id"]?.toLongOrNull()
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.payload?.subject?.toLongOrNull()

        if (chatId == null || userId == null) {
            call.respond(HttpStatusCode.BadRequest, "IDs inválidos")
            return
        }

        val result = listMessagesQuery.execute(chatId, userId)

        result.fold(
            onSuccess = { messages ->
                call.respond(messages.map { it.toWsMessageOut() })
            },
            onFailure = { error ->
                val statusCode = if (error is SecurityException) HttpStatusCode.Forbidden else HttpStatusCode.InternalServerError
                call.respond(statusCode, error.message ?: "Error")
            }
        )
    }

    /**
     * Maneja la conexión WebSocket [WS] /ws/chat/{id}
     */
    suspend fun handleWebSocketConnection(session: DefaultWebSocketServerSession) {
        val chatId = session.call.parameters["id"]?.toLongOrNull()
        val principal = session.call.principal<JWTPrincipal>()
        val userId = principal?.payload?.subject?.toLongOrNull()

        // --- 1. Validación de Entrada ---
        if (chatId == null || userId == null) {
            session.close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No autorizado"))
            return
        }

        // --- 2. Validación de Seguridad ---
        if (!chatRepository.isUserMemberOfChat(userId, chatId)) {
            session.close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No eres miembro"))
            return
        }

        // --- 3. Unirse a la Sala ---
        ChatConnectionManager.join(chatId, session)

        try {
            // --- 4. Bucle de escucha (Mensajes Entrantes) ---
            for (frame in session.incoming) {
                if (frame is Frame.Text) {
                    val frameText = frame.readText()
                    val messageIn = try {
                        Json.decodeFromString<WsMessageIn>(frameText)
                    } catch (e: Exception) { continue }

                    val result = sendMessageUseCase.execute(chatId, userId, messageIn.body)

                    result.onSuccess { savedMessage ->
                        val messageOutJson = Json.encodeToString(
                            WsMessageOut.serializer(),
                            savedMessage.toWsMessageOut()
                        )
                        ChatConnectionManager.broadcast(chatId, messageOutJson)
                    }
                    // Opcional: manejar el result.onFailure enviando un error al emisor
                }
            }
        } catch (e: Exception) {
            println("WS Error para $userId en chat $chatId: ${e.message}")
        } finally {
            // --- 5. Limpiar Conexión ---
            ChatConnectionManager.leave(chatId, session)
        }
    }
}