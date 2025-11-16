package backend.infrastructure.inbound.http.routes

import backend.infrastructure.inbound.http.handler.ChatHandler
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import org.koin.ktor.ext.inject

/**
 * Define las rutas de chat (REST y WebSocket).
 * Delega toda la lógica de manejo al ChatHandler.
 */
fun Route.chatRoutes() {

    // Inyectamos el Handler (que a su vez tiene inyectados los casos de uso)
    val chatHandler: ChatHandler by inject()

    authenticate("auth-jwt") {

        /**
         * Endpoint REST para cargar el HISTORIAL de mensajes.
         */
        get("/chat/{id}/messages") {
            chatHandler.handleGetMessages(call)
        }

        /**
         * Endpoint WEBSOCKET para la comunicación en TIEMPO REAL.
         */
        webSocket("/ws/chat/{id}") {
            chatHandler.handleWebSocketConnection(this)
        }
    }
}