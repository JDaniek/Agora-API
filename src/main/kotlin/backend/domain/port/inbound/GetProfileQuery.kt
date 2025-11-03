package backend.domain.port.inbound

import backend.domain.model.Profile
import backend.domain.model.Specialty

// Contenedor para los datos combinados del perfil
data class ProfileDetails(
    val profile: Profile?, // El perfil puede ser nulo si no se ha creado
    val specialties: List<Specialty>
)

// Puerto de entrada para la consulta
interface GetProfileQuery {
    /**
     * Ejecuta la consulta para obtener los detalles del perfil de un usuario.
     */
    suspend fun execute(userId: Long): ProfileDetails
}
