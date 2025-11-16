package backend.infrastructure.inbound.http.routes

import backend.infrastructure.inbound.http.handler.NotificationHandler
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.auth.*

// Sigue tu patrón: recibe el handler como parámetro
fun Route.notificationRouting(handler: NotificationHandler) {

    authenticate("auth-jwt") {
        route("/notifications") {

            // GET /api/v1/notifications
            get {
                handler.getUserNotifications(call)
            }

            // PATCH /api/v1/notifications/{id}
            patch("/{id}") {
                handler.updateNotificationStatus(call)
            }
        }
    }
}