package backend.infrastructure.inbound.http.routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/health") { call.respondText("OK") }

        route("/api/v1") {
            // Aquí montaremos /auth, /classes, /chats cuando estén listos
        }
    }
}
