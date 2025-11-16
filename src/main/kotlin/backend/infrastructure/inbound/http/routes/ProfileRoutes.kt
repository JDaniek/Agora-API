package backend.infrastructure.inbound.http.routes

import backend.infrastructure.inbound.http.handler.ProfileHandler
import io.ktor.server.routing.*
import io.ktor.server.auth.*
import io.ktor.server.application.* // <-- Import 'call' (implícito en 'get'/'put')

// Rutas específicas para Perfil
fun Route.profileRouting(handler: ProfileHandler) {
    authenticate("auth-jwt") {
        route("/profile") {
            put {
                handler.handleUpdateProfile(call)
            }
            get {
                handler.handleGetProfile(call)
            }
        }
    }
}