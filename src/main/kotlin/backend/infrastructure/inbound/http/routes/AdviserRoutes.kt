package backend.infrastructure.inbound.http.routes

import backend.infrastructure.inbound.http.handler.AdviserHandler
import io.ktor.server.routing.*
import io.ktor.server.auth.*

fun Route.adviserRouting(handler: AdviserHandler) {
    // Todos los alumnos (y asesores) pueden buscar, así que protegemos la ruta.
    authenticate("auth-jwt") {
        route("/advisers") {
            // GET /api/v1/advisers
            get {
                handler.handleSearchAdvisers(call)
            }
        }
    }
}