package backend.infrastructure.inbound.http.routes

import backend.infrastructure.inbound.http.handler.ClassHandler
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Route.classRoutes(handler: ClassHandler) {
// --- ZONA PÚBLICA (Sin Token) ---
    route("/classes") {
        // Nuevo endpoint público
        get("/teachers/{tutorId}") {
            handler.getClassesByTutor(call)
        }
    }
    authenticate("auth-jwt") {
        route("/classes") {

            // POST /api/v1/classes
            post {
                handler.createClass(call)
            }

            // GET /api/v1/classes/mine
            get("/mine") {
                handler.getMyClasses(call)
            }

            // PATCH /api/v1/classes/{id}
            patch("/{id}") {
                handler.updateClass(call)
            }
            // POST /api/v1/classes/{id}/enrollments
            post("/{id}/enrollments") {
                handler.enrollStudent(call)
            }
            // POST listar alumnos de una clase
            get("/{id}/enrollments") {
                handler.getClassEnrollments(call)
            }
            //Mis clases como ALUMNO
            get("/enrolled/mine") {
                handler.getMyEnrolledClasses(call)
            }
            // Eliminar clase
            delete("/{id}") {
                handler.deleteClass(call)
            }

        }
    }
}

