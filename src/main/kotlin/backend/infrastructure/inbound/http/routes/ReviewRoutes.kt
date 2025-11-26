package backend.infrastructure.inbound.http.routes

import backend.infrastructure.inbound.http.handler.ReviewHandler
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Route.reviewRoutes(handler: ReviewHandler) {

    route("/reviews") {

        // Público: ver reseñas de un asesor
        get("/teachers/{teacherId}") {
            handler.getTeacherReviews(call)
        }
        get("/teachers/{teacherId}/summary") {
            handler.getTeacherReviewSummary(call)
        }
        //NUEVO: Reseñas asesor -> alumno (público)
        get("/students/{studentId}") {
            handler.getStudentReviews(call)
        }
        // NUEVO summary público
        get("/students/{studentId}/summary") {
            handler.getStudentReviewSummary(call)
        }

        authenticate("auth-jwt") {
            // Alumno deja reseña a un asesor
            post("/teachers/{teacherId}") {
                handler.createTeacherReview(call)
            }

            // Asesor ve SUS reseñas (como profesor calificado)
            get("/mine") {
                handler.getMyReviews(call)
            }
            //  NUEVO: asesor -> alumno
            post("/students/{studentId}") {
                handler.createStudentReview(call)
            }

            // Alumno ve reseñas que ha recibido
            get("/students/mine") {
                handler.getMyStudentReviews(call)
            }
            // NUEVO: summary del alumno autenticado
            get("/students/mine/summary") {
                handler.getMyStudentReviewSummary(call)
            }
            //NUEVO: últimas reseñas
            get("/students/mine/latest") {
                handler.getMyLatestStudentReviews(call)
            }
        }


    }
}