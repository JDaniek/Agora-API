package backend.infrastructure.inbound.http.routes

import backend.infrastructure.inbound.http.handler.ChatHandler
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*

fun Route.chatRoutes(chatHandler: ChatHandler) {

    authenticate("auth-jwt") {

        get("/chat/{id}/messages") {
            chatHandler.handleGetMessages(call)
        }

        webSocket("/ws/chat/{id}") {
            chatHandler.handleWebSocketConnection(this)
        }
// NUEVA RUTA
        get("/chats/mine") {
            chatHandler.handleGetMyChats(call)
        }
    }


}
