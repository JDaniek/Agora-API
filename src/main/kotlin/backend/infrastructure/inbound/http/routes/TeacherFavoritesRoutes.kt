package backend.infrastructure.inbound.http.routes

import backend.infrastructure.inbound.http.handler.TeacherFavoritesHandler
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Route.teacherFavoritesRoutes(handler: TeacherFavoritesHandler) {
    authenticate("auth-jwt") {
        route("/favorites/teachers") {

            // GET /api/v1/favorites/teachers
            get {
                handler.getMyFavoriteTeachers(call)
            }

            // POST /api/v1/favorites/teachers/{teacherId}
            post("{teacherId}") {
                handler.addFavorite(call)
            }

            // DELETE /api/v1/favorites/teachers/{teacherId}
            delete("{teacherId}") {
                handler.removeFavorite(call)
            }
        }
    }
}
