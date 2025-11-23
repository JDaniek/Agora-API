package backend.infrastructure.inbound.http.routes

import backend.infrastructure.inbound.http.handler.AuthHandler
import backend.infrastructure.inbound.http.handler.MediaHandler
import backend.infrastructure.inbound.http.handler.ProfileHandler
import backend.infrastructure.inbound.http.handler.AdviserHandler
import backend.infrastructure.inbound.http.handler.NotificationHandler
import backend.infrastructure.inbound.http.handler.ChatHandler // <--- IMPORTA EL CHAT HANDLER
import backend.infrastructure.inbound.http.handler.ClassHandler
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.authRouting(handler: AuthHandler) {
    route("/auth") {
        post("/register") { handler.handleRegister(call) }
        post("/login") { handler.handleLogin(call) }
    }
}

fun Application.configureRouting() {
    val authHandler by inject<AuthHandler>()
    val profileHandler by inject<ProfileHandler>()
    val mediaHandler by inject<MediaHandler>()
    val adviserHandler by inject<AdviserHandler>()
    val notificationHandler by inject<NotificationHandler>()
    val classHandler: ClassHandler by inject()

    // --- INYECTAMOS EL CHAT HANDLER AQUÍ ---
    val chatHandler by inject<ChatHandler>()
    // ---------------------------------------

    routing {
        get("/") {
            call.respondText("Welcome to Agora API!")
        }

        route("/api/v1") {
            authRouting(authHandler)
            profileRouting(profileHandler)
            mediaRouting(mediaHandler)
            adviserRouting(adviserHandler)
            notificationRouting(notificationHandler)
            classRoutes(classHandler)

            // --- SE LO PASAMOS COMO PARÁMETRO ---
            chatRoutes(chatHandler)
            // ------------------------------------
        }
    }
}