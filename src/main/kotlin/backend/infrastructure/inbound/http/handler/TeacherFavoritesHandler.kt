package backend.infrastructure.inbound.http.handler

import backend.domain.port.outbound.TeacherFavoritesRepository
import backend.infrastructure.inbound.http.dto.AdviserCardResponse
import backend.infrastructure.inbound.http.dto.toDTO
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

class TeacherFavoritesHandler(
    private val favoritesRepo: TeacherFavoritesRepository
) {

    /**
     * GET /api/v1/favorites/teachers
     * Devuelve la lista de asesores favoritos del alumno autenticado
     */
    suspend fun getMyFavoriteTeachers(call: ApplicationCall) {
        val principal = call.principal<JWTPrincipal>()
        val studentId = principal?.payload?.subject?.toLongOrNull()
            ?: return call.respond(HttpStatusCode.Unauthorized, "Token inválido")

        val cards = favoritesRepo.findFavoriteAdvisersForStudent(studentId)

        // Usamos el mapper AdviserCard.toDTO() que ya definiste
        val dto: List<AdviserCardResponse> = cards.map { it.toDTO() }

        call.respond(HttpStatusCode.OK, dto)
    }

    /**
     * POST /api/v1/favorites/teachers/{teacherId}
     * Marca a un profesor como favorito
     */
    suspend fun addFavorite(call: ApplicationCall) {
        val principal = call.principal<JWTPrincipal>()
        val studentId = principal?.payload?.subject?.toLongOrNull()
            ?: return call.respond(HttpStatusCode.Unauthorized, "Token inválido")

        val teacherId = call.parameters["teacherId"]?.toLongOrNull()
            ?: return call.respond(HttpStatusCode.BadRequest, "teacherId inválido")

        favoritesRepo.addFavorite(studentId, teacherId)
        call.respond(HttpStatusCode.NoContent)
    }

    /**
     * DELETE /api/v1/favorites/teachers/{teacherId}
     * Quita a un profesor de favoritos
     */
    suspend fun removeFavorite(call: ApplicationCall) {
        val principal = call.principal<JWTPrincipal>()
        val studentId = principal?.payload?.subject?.toLongOrNull()
            ?: return call.respond(HttpStatusCode.Unauthorized, "Token inválido")

        val teacherId = call.parameters["teacherId"]?.toLongOrNull()
            ?: return call.respond(HttpStatusCode.BadRequest, "teacherId inválido")

        favoritesRepo.removeFavorite(studentId, teacherId)
        call.respond(HttpStatusCode.NoContent)
    }
}
