package backend.domain.port.outbound

import backend.domain.model.Specialty

interface SpecialtyRepository {
    // TODO: Implementar métodos para 'findAll', 'findById', etc.

    /**
     * Busca todas las especialidades asociadas a un ID de usuario.
     */
    suspend fun findByUserId(userId: Long): List<Specialty>
}
