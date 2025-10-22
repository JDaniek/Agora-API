package backend.infrastructure.inbound.http.routes

import backend.infrastructure.inbound.http.handler.AuthHandler
// import backend.infrastructure.inbound.http.handler.ChatHandler  // <-- Comentado
// import backend.infrastructure.inbound.http.handler.ClassHandler // <-- Comentado
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

// Define la ruta específica para Auth
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
    // Inyecta los handlers desde Koin
    val authHandler by inject<AuthHandler>()

    // TODO: Descomenta esto cuando implementes ClassHandler
    // val classHandler by inject<ClassHandler>() // <-- Comentado

    // TODO: Descomenta esto cuando implementes ChatHandler
    // val chatHandler by inject<ChatHandler>() // <-- Comentado

    routing {
        get("/") {
            call.respondText("Welcome to Agora API!")
        }

        // API v1
        route("/api/v1") {
            // Registra las rutas de autenticación
            authRouting(authHandler)

            // TODO: Descomenta tus otras rutas
            // classRouting(classHandler)
            // chatRouting(chatHandler)
        }
    }
}