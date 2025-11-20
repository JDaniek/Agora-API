package backend.infrastructure.inbound.http.routes

import backend.infrastructure.inbound.http.handler.ChatHandler
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
// Borra el import de koin (org.koin.ktor.ext.inject)

/**
 * AHORA RECIBE EL HANDLER POR PARÁMETRO
 * Esto evita el error de versión de Koin.
 */
fun Route.chatRoutes(chatHandler: ChatHandler) { // <--- CAMBIO AQUÍ

    // ELIMINAMOS ESTA LÍNEA QUE CAUSABA EL ERROR:
    // val chatHandler: ChatHandler by inject()

    authenticate("auth-jwt") {

        get("/chat/{id}/messages") {
            chatHandler.handleGetMessages(call)
        }

        webSocket("/ws/chat/{id}") {
            chatHandler.handleWebSocketConnection(this)
        }
    }
}