package backend.infrastructure.inbound.http.handler

import backend.domain.port.inbound.GetProfileQuery // <-- Asegúrate de importar
import backend.domain.port.inbound.UpdateProfileUseCase
import backend.infrastructure.inbound.http.dto.UpdateProfileRequest
import backend.infrastructure.inbound.http.dto.toResponseDTO
import backend.infrastructure.security.JwtConfig // <-- Ruta de import correcta
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.*

// ¡¡CORRECCIÓN AQUÍ!! El constructor ahora recibe DOS casos de uso
class ProfileHandler(
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val getProfileQuery: GetProfileQuery
) {
    /**
     * Manejador para PUT /api/v1/profile (Actualizar perfil)
     * --- VERSIÓN CORREGIDA ---
     */
    suspend fun handleUpdateProfile(call: ApplicationCall) {
        val request = call.receive<UpdateProfileRequest>()
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.payload?.subject?.toLongOrNull()
            ?: return call.respond(HttpStatusCode.Unauthorized, "Invalid token")

        val command = UpdateProfileUseCase.UpdateProfileCommand(
            userId = userId,
            description = request.description,
            photoUrl = request.photoUrl,
            city = request.city,
            stateCode = request.stateCode,
            level = request.level,
            specialtyIds = request.specialtyIds
        )

        try {
            // 1. Ejecuta el guardado (Actualiza la BD)
            updateProfileUseCase.execute(command)

            // --- ¡¡ARREGLO!! ---
            // 2. Después de guardar, vuelve a consultar los datos completos
            val profileDetails = getProfileQuery.execute(userId)

            // 3. Si por alguna razón no lo encuentra (no debería pasar)
            val profile = profileDetails.profile
                ?: return call.respond(HttpStatusCode.NotFound, "Profile could not be found after update.")

            // 4. Responde con el DTO serializable (ProfileResponse)
            val responseDTO = profile.toResponseDTO(profileDetails.specialties)
            call.respond(HttpStatusCode.OK, responseDTO)
            // --- FIN DEL ARREGLO ---

        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Error updating profile: ${e.message}")
        }
    }

    /**
     * Manejador para GET /api/v1/profile (Obtener perfil)
     */
    suspend fun handleGetProfile(call: ApplicationCall) {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.payload?.subject?.toLongOrNull()
            ?: return call.respond(HttpStatusCode.Unauthorized, "Invalid token")

        try {
            val profileDetails = getProfileQuery.execute(userId)

            val profile = profileDetails.profile
                ?: return call.respond(HttpStatusCode.NotFound, "Profile not created yet.")

            val responseDTO = profile.toResponseDTO(profileDetails.specialties)
            call.respond(HttpStatusCode.OK, responseDTO)

        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Error getting profile: ${e.message}")
        }
    }

}