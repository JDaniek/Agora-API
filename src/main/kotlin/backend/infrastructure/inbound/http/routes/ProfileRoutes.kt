package backend.infrastructure.inbound.http.routes

import backend.infrastructure.inbound.http.handler.ProfileHandler
import io.ktor.server.routing.*
import io.ktor.server.auth.*

// Rutas específicas para Perfil
fun Route.profileRouting(handler: ProfileHandler) {
    // ¡¡IMPORTANTE: Protegemos todas las rutas de /profile!!
    authenticate("auth-jwt") { // <--- Nombre de tu configuración de JWT
        route("/profile") {
            // PUT /api/v1/profile
            put {
                handler.handleUpdateProfile(call)
            }

            // GET /api/v1/profile
            get {
               handler.handleGetProfile(call)
             }
        }
    }
}
