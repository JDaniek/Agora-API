package backend.infrastructure.inbound.http.routes

import backend.infrastructure.inbound.http.handler.AdviserHandler
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.auth.*

fun Route.adviserRouting(handler: AdviserHandler) {

    authenticate("auth-jwt") {
        route("/advisers") {

            // GET /api/v1/advisers
            get {
                handler.handleSearchAdvisers(call)
            }

            // POST /api/v1/advisers/{id}/contact
            post("/{id}/contact") {
                handler.requestContact(call)
            }
        }
    }
}