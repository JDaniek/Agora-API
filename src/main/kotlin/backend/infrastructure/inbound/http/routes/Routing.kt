package backend.infrastructure.inbound.http.routes

import backend.infrastructure.inbound.http.handler.AuthHandler
import backend.infrastructure.inbound.http.handler.MediaHandler
import backend.infrastructure.inbound.http.handler.ProfileHandler
import backend.infrastructure.inbound.http.handler.AdviserHandler
import backend.infrastructure.inbound.http.handler.NotificationHandler // <-- ¡NUEVO IMPORT!
// import backend.infrastructure.inbound.http.handler.ChatHandler
// import backend.infrastructure.inbound.http.handler.ClassHandler
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

// (Tu función 'authRouting' está perfecta)
fun Route.authRouting(handler: AuthHandler) {
    route("/auth") {
        post("/register") {
            handler.handleRegister(call)
        }
        post("/login") {
            handler.handleLogin(call)
        }
    }
}

// Configuración principal
fun Application.configureRouting() {
    // Inyecta los handlers desde Koin (tu patrón original)
    val authHandler by inject<AuthHandler>()
    val profileHandler by inject<ProfileHandler>()
    val mediaHandler by inject<MediaHandler>()
    val adviserHandler by inject<AdviserHandler>()
    val notificationHandler by inject<NotificationHandler>() // <-- ¡NUEVA INYECCIÓN!

    routing {
        get("/") {
            call.respondText("Welcome to Agora API!")
        }

        // API v1
        route("/api/v1") {
            // Registra las rutas de autenticación
            authRouting(authHandler)
            profileRouting(profileHandler)
            mediaRouting(mediaHandler)
            adviserRouting(adviserHandler)
            notificationRouting(notificationHandler) // <-- ¡NUEVA RUTA AÑADIDA!

            // classRouting(classHandler)
            // chatRouting(chatHandler)
        }
    }
}