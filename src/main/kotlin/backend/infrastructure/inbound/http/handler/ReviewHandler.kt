package backend.infrastructure.inbound.http.handler

import backend.domain.port.inbound.CreateStudentReviewUseCase
import backend.domain.port.inbound.CreateTeacherReviewUseCase
import backend.domain.port.inbound.GetStudentReviewSummaryQuery
import backend.domain.port.inbound.GetStudentReviewsForStudentQuery
import backend.domain.port.inbound.GetTeacherReviewSummaryQuery
import backend.domain.port.inbound.GetTeacherReviewsQuery
import backend.infrastructure.inbound.http.dto.reviews.CreateReviewRequest
import backend.infrastructure.inbound.http.mapper.reviews.toResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import backend.infrastructure.inbound.http.dto.reviews.ReviewCreatedResponse
import backend.infrastructure.inbound.http.mapper.reviews.toStudentResponse
import backend.infrastructure.inbound.http.mapper.reviews.toStudentSummaryResponse

class ReviewHandler(
    private val createTeacherReviewUseCase: CreateTeacherReviewUseCase,
    private val getTeacherReviewsQuery: GetTeacherReviewsQuery,
    private val getTeacherReviewSummaryQuery: GetTeacherReviewSummaryQuery,
    private val createStudentReviewUseCase: CreateStudentReviewUseCase,
    private val getStudentReviewsForStudentQuery: GetStudentReviewsForStudentQuery,
    private val getStudentReviewSummaryQuery: GetStudentReviewSummaryQuery
) {

    /**
     * POST /api/v1/reviews/teachers/{teacherId}
     * Alumno autenticado deja una reseña a un asesor.
     */
    suspend fun createTeacherReview(call: ApplicationCall) {
        val principal = call.principal<JWTPrincipal>()
        val studentId = principal?.payload?.subject?.toLongOrNull()

        if (studentId == null) {
            call.respond(HttpStatusCode.Unauthorized, "Token inválido")
            return
        }

        val teacherId = call.parameters["teacherId"]?.toLongOrNull()
        if (teacherId == null) {
            call.respond(HttpStatusCode.BadRequest, "El ID del asesor debe ser numérico")
            return
        }

        val request = try {
            call.receive<CreateReviewRequest>()
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, "Body inválido")
            return
        }

        val result = createTeacherReviewUseCase.execute(
            CreateTeacherReviewUseCase.Command(
                studentId = studentId,
                teacherId = teacherId,
                rating = request.rating,
                comment = request.comment
            )
        )

        result.onSuccess { review ->
            call.respond(
                HttpStatusCode.Created,
                ReviewCreatedResponse(
                    message = "Reseña creada",
                    reviewId = review.id
                )
            )
        }.onFailure { e ->
            val status = when (e) {
                is IllegalArgumentException -> HttpStatusCode.BadRequest
                is IllegalStateException -> HttpStatusCode.Conflict
                else -> HttpStatusCode.InternalServerError
            }
            call.respond(status, mapOf("error" to (e.message ?: "Error al crear la reseña")))
        }
    }

    /**
     * GET /api/v1/reviews/teachers/{teacherId}
     * Lista las reseñas de un asesor (público o autenticado).
     */
    suspend fun getTeacherReviews(call: ApplicationCall) {
        val teacherId = call.parameters["teacherId"]?.toLongOrNull()
        if (teacherId == null) {
            call.respond(HttpStatusCode.BadRequest, "El ID del asesor debe ser numérico")
            return
        }

        val result = getTeacherReviewsQuery.getForTeacher(teacherId)

        result.onSuccess { list ->
            call.respond(HttpStatusCode.OK, list.map { it.toResponse() })
        }.onFailure { e ->
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to (e.message ?: "Error al obtener reseñas"))
            )
        }
    }

    /**
     * GET /api/v1/reviews/mine
     * El asesor autenticado ve sus propias reseñas.
     */
    suspend fun getMyReviews(call: ApplicationCall) {
        val principal = call.principal<JWTPrincipal>()
        val teacherId = principal?.payload?.subject?.toLongOrNull()

        if (teacherId == null) {
            call.respond(HttpStatusCode.Unauthorized, "Token inválido")
            return
        }

        val result = getTeacherReviewsQuery.getForTeacher(teacherId)

        result.onSuccess { list ->
            call.respond(HttpStatusCode.OK, list.map { it.toResponse() })
        }.onFailure { e ->
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to (e.message ?: "Error al obtener mis reseñas"))
            )
        }
    }

    /**
     * GET /api/v1/reviews/teachers/{teacherId}/summary
     * Devuelve promedio y total de reseñas de un asesor.
     */
    suspend fun getTeacherReviewSummary(call: ApplicationCall) {
        val teacherId = call.parameters["teacherId"]?.toLongOrNull()
        if (teacherId == null) {
            call.respond(HttpStatusCode.BadRequest, "El ID del asesor debe ser numérico")
            return
        }

        val result = getTeacherReviewSummaryQuery.getSummary(teacherId)

        result.onSuccess { summary ->
            call.respond(HttpStatusCode.OK, summary.toResponse())
        }.onFailure { e ->
            val status = when (e) {
                is NoSuchElementException -> HttpStatusCode.NotFound
                else -> HttpStatusCode.InternalServerError
            }
            call.respond(
                status,
                mapOf("error" to (e.message ?: "Error al obtener el resumen de reseñas"))
            )
        }
    }

//Seccion de alumnos
    /**
     * POST /api/v1/reviews/students/{studentId}
     * Asesor autenticado deja una reseña a un alumno.
     */
    suspend fun createStudentReview(call: ApplicationCall) {
        val principal = call.principal<JWTPrincipal>()
        val teacherId = principal?.payload?.subject?.toLongOrNull()

        if (teacherId == null) {
            call.respond(HttpStatusCode.Unauthorized, "Token inválido")
            return
        }

        val studentId = call.parameters["studentId"]?.toLongOrNull()
        if (studentId == null) {
            call.respond(HttpStatusCode.BadRequest, "El ID del alumno debe ser numérico")
            return
        }

        val request = try {
            call.receive<CreateReviewRequest>() // mismo DTO: rating + comment
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, "Body inválido")
            return
        }

        val result = createStudentReviewUseCase.execute(
            CreateStudentReviewUseCase.Command(
                teacherId = teacherId,
                studentId = studentId,
                rating = request.rating,
                comment = request.comment
            )
        )

        result.onSuccess { review ->
            call.respond(
                HttpStatusCode.Created,
                ReviewCreatedResponse(
                    message = "Reseña creada para el alumno",
                    reviewId = review.id
                )
            )
        }.onFailure { e ->
            val status = when (e) {
                is IllegalArgumentException -> HttpStatusCode.BadRequest
                is IllegalStateException -> HttpStatusCode.Conflict
                else -> HttpStatusCode.InternalServerError
            }
            call.respond(status, mapOf("error" to (e.message ?: "Error al crear la reseña de alumno")))
        }
    }

    /**
     * GET /api/v1/reviews/students/{studentId}
     * Lista pública de reseñas que un alumno ha recibido.
     */
    suspend fun getStudentReviews(call: ApplicationCall) {
        val studentId = call.parameters["studentId"]?.toLongOrNull()
        if (studentId == null) {
            call.respond(HttpStatusCode.BadRequest, "El ID del alumno debe ser numérico")
            return
        }

        val result = getStudentReviewsForStudentQuery.getForStudent(studentId)

        result.onSuccess { list ->
            call.respond(HttpStatusCode.OK, list.map { it.toStudentResponse() })
        }.onFailure { e ->
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to (e.message ?: "Error al obtener reseñas del alumno"))
            )
        }
    }

    /**
     * GET /api/v1/reviews/students/mine
     * El alumno autenticado ve las reseñas que ha recibido.
     */
    suspend fun getMyStudentReviews(call: ApplicationCall) {
        val principal = call.principal<JWTPrincipal>()
        val studentId = principal?.payload?.subject?.toLongOrNull()

        if (studentId == null) {
            call.respond(HttpStatusCode.Unauthorized, "Token inválido")
            return
        }

        val result = getStudentReviewsForStudentQuery.getForStudent(studentId)

        result.onSuccess { list ->
            call.respond(HttpStatusCode.OK, list.map { it.toStudentResponse() })
        }.onFailure { e ->
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to (e.message ?: "Error al obtener mis reseñas como alumno"))
            )
        }
    }

    /**
     * GET /api/v1/reviews/students/{studentId}/summary
     * Summary público de un alumno (promedio y total reseñas).
     */
    suspend fun getStudentReviewSummary(call: ApplicationCall) {
        val studentId = call.parameters["studentId"]?.toLongOrNull()
        if (studentId == null) {
            call.respond(HttpStatusCode.BadRequest, "El ID del alumno debe ser numérico")
            return
        }

        val result = getStudentReviewSummaryQuery.getSummary(studentId)

        result.onSuccess { summary ->
            if (summary == null || summary.totalReviews == 0) {
                // Puedes devolver todo en cero si no tiene reseñas
                call.respond(
                    HttpStatusCode.OK,
                    mapOf(
                        "studentId" to studentId,
                        "averageRating" to 0.0,
                        "totalReviews" to 0
                    )
                )
            } else {
                call.respond(HttpStatusCode.OK, summary.toStudentSummaryResponse())
            }
        }.onFailure { e ->
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to (e.message ?: "Error al obtener el resumen de reseñas del alumno"))
            )
        }
    }

    /**
     * GET /api/v1/reviews/students/mine/summary
     * Summary para el alumno autenticado.
     */
    suspend fun getMyStudentReviewSummary(call: ApplicationCall) {
        val principal = call.principal<JWTPrincipal>()
        val studentId = principal?.payload?.subject?.toLongOrNull()

        if (studentId == null) {
            call.respond(HttpStatusCode.Unauthorized, "Token inválido")
            return
        }

        val result = getStudentReviewSummaryQuery.getSummary(studentId)

        result.onSuccess { summary ->
            if (summary == null || summary.totalReviews == 0) {
                call.respond(
                    HttpStatusCode.OK,
                    mapOf(
                        "studentId" to studentId,
                        "averageRating" to 0.0,
                        "totalReviews" to 0
                    )
                )
            } else {
                call.respond(HttpStatusCode.OK, summary.toStudentSummaryResponse())
            }
        }.onFailure { e ->
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to (e.message ?: "Error al obtener el resumen de mis reseñas"))
            )
        }
    }

    /**
     * GET /api/v1/reviews/students/mine/latest?limit=3
     * Últimas reseñas que profesores han dejado sobre el alumno autenticado.
     */
    suspend fun getMyLatestStudentReviews(call: ApplicationCall) {
        val principal = call.principal<JWTPrincipal>()
        val studentId = principal?.payload?.subject?.toLongOrNull()

        if (studentId == null) {
            call.respond(HttpStatusCode.Unauthorized, "Token inválido")
            return
        }

        val limit = call.request.queryParameters["limit"]
            ?.toIntOrNull()
            ?.coerceIn(1, 50)  // por si acaso
            ?: 3

        val result = getStudentReviewsForStudentQuery.getForStudent(studentId)

        result.onSuccess { list ->
            // Ordenamos por fecha descendente por si el repo no lo garantiza
            val latest = list
                .sortedByDescending { it.createdAt }
                .take(limit)

            call.respond(
                HttpStatusCode.OK,
                latest.map { it.toStudentResponse() }
            )
        }.onFailure { e ->
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to (e.message ?: "Error al obtener mis últimas reseñas"))
            )
        }
    }
}