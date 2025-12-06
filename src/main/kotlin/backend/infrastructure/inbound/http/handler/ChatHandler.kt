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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Maneja toda la lógica de negocio para las rutas de Chat (REST y WebSocket).
 * Recibe los casos de uso por inyección de dependencias (Koin)
 */
class ChatHandler(
    private val listMessagesQuery: GetChatMessagesQuery,
    private val sendMessageUseCase: SendMessageUseCase,
    private val chatRepository: ChatRepository
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
                val statusCode =
                    if (error is SecurityException) HttpStatusCode.Forbidden else HttpStatusCode.InternalServerError
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

        //TRAMPA 1: ¿QUIÉN TOCA LA PUERTA? ---
        println("🛑 DEBUG HANDLER: === Intento de conexión ===")
        println("🛑 DEBUG HANDLER: Chat ID recibido: '$chatId'")
        println("🛑 DEBUG HANDLER: User ID del Token: '$userId'")

        // 1. Validación de Entrada
        if (chatId == null || userId == null) {
            println("🛑 DEBUG HANDLER: Falló por IDs nulos. Cerrando.")
            session.close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No autorizado"))
            return
        }

        // TRAMPA 2: PREGUNTANDO A LA DB ---
        println("🛑 DEBUG HANDLER: Consultando repositorio isUserMemberOfChat...")

        val isMember = try {
            chatRepository.isUserMemberOfChat(userId, chatId)
        } catch (e: Exception) {
            println("🛑 DEBUG HANDLER: 💥 EXCEPCIÓN AL CONSULTAR DB: ${e.message}")
            e.printStackTrace()
            false
        }

        println("🛑 DEBUG HANDLER: Resultado del Repo: $isMember")

        // 2. Validación de Seguridad
        if (!isMember) {
            println("🛑 DEBUG HANDLER: ¡Acceso Denegado! Cerrando sesión.")
            session.close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No eres miembro"))
            return
        }

        // TRAMPA 3: ÉXITO ---
        println("🛑 DEBUG HANDLER: Acceso concedido. Uniendo al ConnectionManager...")

        // 3. Unirse a la Sala
        ChatConnectionManager.join(chatId, session)

        try {
            // 4. Bucle de escucha (Mensajes Entrantes)
            for (frame in session.incoming) {
                if (frame is Frame.Text) {
                    val frameText = frame.readText()
                    println("🛑 DEBUG HANDLER: Mensaje recibido: $frameText") // Log extra

                    val messageIn = try {
                        Json.decodeFromString<WsMessageIn>(frameText)
                    } catch (e: Exception) {
                        println("🛑 DEBUG HANDLER: Error deserializando JSON: ${e.message}")
                        continue
                    }

                    val result = sendMessageUseCase.execute(chatId, userId, messageIn.body)

                    result.onSuccess { savedMessage ->
                        val messageOutJson = Json.encodeToString(savedMessage.toWsMessageOut())
                        ChatConnectionManager.broadcast(chatId, messageOutJson)
                    }.onFailure { e ->
                        println("🛑 DEBUG HANDLER: Error guardando mensaje: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            println("🛑 DEBUG HANDLER: Error general en WebSocket: ${e.message}")
        } finally {
            // 5. Limpiar Conexión
            println("🛑 DEBUG HANDLER: Cliente desconectado. Limpiando recursos.")
            ChatConnectionManager.leave(chatId, session)
        }
    }


    /**
     * GET /api/v1/chats/mine
     */
    suspend fun handleGetMyChats(call: ApplicationCall) {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.payload?.subject?.toLongOrNull()

        if (userId == null) {
            call.respond(HttpStatusCode.Unauthorized, "Token inválido")
            return
        }

        val result = chatRepository.getChatsForUser(userId)

        result.onSuccess { chats ->
            call.respond(HttpStatusCode.OK, chats)
        }.onFailure { e ->
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
        }
    }


}