package backend.infrastructure.inbound.http.handler

import backend.domain.port.inbound.*
import backend.domain.port.outbound.ClassRepository
import backend.infrastructure.inbound.http.dto.classes.CreateClassRequest
import backend.infrastructure.inbound.http.dto.classes.EnrollStudentRequest
import backend.infrastructure.inbound.http.dto.classes.UpdateClassRequest
import backend.infrastructure.inbound.http.mapper.classes.toResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import java.time.LocalDate
import backend.infrastructure.inbound.http.mapper.classes.toResponse
import backend.infrastructure.inbound.http.mapper.classes.toStudentResponse


class ClassHandler(
    private val createClassUseCase: CreateClassUseCase,
    private val classRepository: ClassRepository,
    private val updateClassUseCase: UpdateClassUseCase,
    private val enrollStudentInClassUseCase: EnrollStudentInClassUseCase,
    private val getClassEnrollmentsQuery: GetClassEnrollmentsQuery,
    private val getStudentEnrolledClassesQuery: GetStudentEnrolledClassesQuery,
    private val deleteClassUseCase: DeleteClassUseCase
) {

    /**
     * POST /api/v1/classes
     * Crea una clase para el asesor autenticado.
     */
    suspend fun createClass(call: ApplicationCall) {
        val principal = call.principal<JWTPrincipal>()
        val tutorId = principal?.payload?.subject?.toLongOrNull()

        if (tutorId == null) {
            call.respond(HttpStatusCode.Unauthorized, "Token inválido")
            return
        }

        val request = call.receive<CreateClassRequest>()

        val classDate = try {
            LocalDate.parse(request.classDate) // "YYYY-MM-DD"
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, "Fecha inválida. Usa formato YYYY-MM-DD.")
            return
        }

        val command = CreateClassUseCase.CreateClassCommand(
            tutorId = tutorId,
            title = request.title,
            description = request.description,
            classDate = classDate,
            capacityPerSlot = request.capacityPerSlot,
            specialtyId = request.specialtyId
        )

        val result = createClassUseCase.execute(command)

        result.onSuccess { cls ->
            call.respond(HttpStatusCode.Created, cls.toResponse())
        }.onFailure { e ->
            call.respond(HttpStatusCode.BadRequest, e.message ?: "Error al crear la clase")
        }
    }

    /**
     * GET /api/v1/classes/mine
     * Lista las clases del asesor autenticado.
     */
    suspend fun getMyClasses(call: ApplicationCall) {
        val principal = call.principal<JWTPrincipal>()
        val tutorId = principal?.payload?.subject?.toLongOrNull()

        if (tutorId == null) {
            call.respond(HttpStatusCode.Unauthorized, "Token inválido")
            return
        }

        val classes = classRepository.findByTutor(tutorId)
        call.respond(classes.map { it.toResponse() })
    }

    /**
     * PATCH /api/v1/classes/{id}
     * Actualiza parcialmente una clase del tutor.
     */
    suspend fun updateClass(call: ApplicationCall) {
        val principal = call.principal<JWTPrincipal>()
        val tutorId = principal?.payload?.subject?.toLongOrNull()

        if (tutorId == null) {
            call.respond(HttpStatusCode.Unauthorized, "Token inválido")
            return
        }

        val classId = call.parameters["id"]?.toLongOrNull()
        if (classId == null) {
            call.respond(HttpStatusCode.BadRequest, "ID de clase inválido")
            return
        }

        val request = call.receive<UpdateClassRequest>()

        val parsedDate: LocalDate? = if (request.classDate != null) {
            try {
                LocalDate.parse(request.classDate)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Fecha inválida. Usa formato YYYY-MM-DD.")
                return
            }
        } else {
            null
        }

        val command = UpdateClassUseCase.UpdateClassCommand(
            classId = classId,
            tutorId = tutorId,
            title = request.title,
            description = request.description,
            classDate = parsedDate,
            capacityPerSlot = request.capacityPerSlot,
            specialtyId = request.specialtyId,
            isActive = request.isActive
        )

        val result = updateClassUseCase.execute(command)

        result.onSuccess { updated ->
            call.respond(HttpStatusCode.OK, updated.toResponse())
        }.onFailure { e ->
            val status = when (e) {
                is NoSuchElementException -> HttpStatusCode.NotFound
                is IllegalAccessException -> HttpStatusCode.Forbidden
                is IllegalArgumentException -> HttpStatusCode.BadRequest
                else -> HttpStatusCode.InternalServerError
            }
            call.respond(status, e.message ?: "Error al actualizar la clase")
        }
    }

    /**
     * POST /api/v1/classes/{id}/enrollments
     * Inscribe un alumno en una clase del tutor autenticado.
     */
    suspend fun enrollStudent(call: ApplicationCall) {
        val principal = call.principal<JWTPrincipal>()
        val tutorId = principal?.payload?.subject?.toLongOrNull()

        if (tutorId == null) {
            call.respond(HttpStatusCode.Unauthorized, "Token inválido")
            return
        }

        val classId = call.parameters["id"]?.toLongOrNull()
        if (classId == null) {
            call.respond(HttpStatusCode.BadRequest, "El ID de la clase debe ser numérico")
            return
        }

        val request = try {
            call.receive<EnrollStudentRequest>()
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, "Body inválido")
            return
        }

        val result = enrollStudentInClassUseCase.execute(
            EnrollStudentInClassUseCase.Command(
                tutorId = tutorId, classId = classId, studentId = request.studentId
            )
        )

        result.onSuccess {
            call.respond(
                HttpStatusCode.Created, mapOf("message" to "Alumno inscrito en la clase")
            )
        }.onFailure { e ->
            val status = when (e) {
                is SecurityException -> HttpStatusCode.Forbidden
                is IllegalArgumentException -> HttpStatusCode.NotFound
                is IllegalStateException -> HttpStatusCode.Conflict
                else -> HttpStatusCode.InternalServerError
            }

            call.respond(status, mapOf("error" to (e.message ?: "Error al inscribir al alumno")))
        }
    }


    /**
     * GET /api/v1/classes/{id}/enrollments
     * Lista los alumnos inscritos en una clase del tutor autenticado.
     */
    suspend fun getClassEnrollments(call: ApplicationCall) {
        val principal = call.principal<JWTPrincipal>()
        val tutorId = principal?.payload?.subject?.toLongOrNull()

        if (tutorId == null) {
            call.respond(HttpStatusCode.Unauthorized, "Token inválido")
            return
        }

        val classId = call.parameters["id"]?.toLongOrNull()
        if (classId == null) {
            call.respond(HttpStatusCode.BadRequest, "El ID de la clase debe ser numérico")
            return
        }

        val result = getClassEnrollmentsQuery.getEnrollments(tutorId, classId)

        result.onSuccess { list ->
            call.respond(list.map { it.toResponse() })
        }.onFailure { e ->
            val status = when (e) {
                is NoSuchElementException -> HttpStatusCode.NotFound
                is SecurityException -> HttpStatusCode.Forbidden
                else -> HttpStatusCode.InternalServerError
            }
            call.respond(status, mapOf("error" to (e.message ?: "Error al obtener inscripciones")))
        }
    }

    /**
     * GET /api/v1/classes/enrolled/mine
     * Lista las clases en las que el ALUMNO autenticado está inscrito.
     */
    suspend fun getMyEnrolledClasses(call: ApplicationCall) {
        val principal = call.principal<JWTPrincipal>()
        val studentId = principal?.payload?.subject?.toLongOrNull()

        if (studentId == null) {
            call.respond(HttpStatusCode.Unauthorized, "Token inválido")
            return
        }

        val result = getStudentEnrolledClassesQuery.getForStudent(studentId)

        result.onSuccess { list ->
            call.respond(HttpStatusCode.OK, list.map { it.toStudentResponse() })
        }.onFailure { e ->
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to (e.message ?: "Error al obtener las clases del alumno"))
            )
        }
    }

    /**
     * DELETE /api/v1/classes/{id}
     * Elimina una clase (solo el tutor dueño puede hacerlo).
     */
    suspend fun deleteClass(call: ApplicationCall) {
        val principal = call.principal<JWTPrincipal>()
        val tutorId = principal?.payload?.subject?.toLongOrNull()

        if (tutorId == null) {
            call.respond(HttpStatusCode.Unauthorized, "Token inválido")
            return
        }

        val classId = call.parameters["id"]?.toLongOrNull()
        if (classId == null) {
            call.respond(HttpStatusCode.BadRequest, "ID de clase inválido")
            return
        }

        val result = deleteClassUseCase.execute(
            DeleteClassUseCase.Command(
                tutorId = tutorId,
                classId = classId
            )
        )

        result.onSuccess {
            call.respond(HttpStatusCode.NoContent)
        }.onFailure { e ->
            val status = when (e) {
                is NoSuchElementException -> HttpStatusCode.NotFound
                is IllegalAccessException, is SecurityException -> HttpStatusCode.Forbidden
                else -> HttpStatusCode.InternalServerError
            }
            call.respond(status, mapOf("error" to (e.message ?: "Error al eliminar la clase")))
        }
    }

    /**
     * GET /api/v1/classes/teachers/{tutorId}
     * Endpoint PÚBLICO para listar clases disponibles de un profesor.
     */
    suspend fun getClassesByTutor(call: ApplicationCall) {
        val tutorId = call.parameters["tutorId"]?.toLongOrNull()

        if (tutorId == null) {
            call.respond(HttpStatusCode.BadRequest, "El ID del profesor es inválido")
            return
        }

        // 1. Reutilizamos el método que ya tienes en el repositorio
        val allClasses = classRepository.findByTutor(tutorId)

        // 2. Filtramos: Solo mostramos las clases ACTIVAS al público
        // (Opcional: también podrías filtrar por fecha para no mostrar clases pasadas)
        val activeClasses = allClasses.filter {
            it.isActive == true // Asumiendo que ClassSession tiene este campo
        }

        call.respond(HttpStatusCode.OK, activeClasses.map { it.toResponse() })
    }
}
