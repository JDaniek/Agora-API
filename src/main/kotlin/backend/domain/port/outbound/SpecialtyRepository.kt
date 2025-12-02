package backend.domain.port.outbound

import backend.domain.model.Specialty

interface SpecialtyRepository {
    /**
     * Busca todas las especialidades asociadas a un ID de usuario.
     */
    suspend fun findByUserId(userId: Long): List<Specialty>
}
