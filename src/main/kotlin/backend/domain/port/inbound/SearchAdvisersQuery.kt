package backend.domain.port.inbound

import backend.domain.model.AdviserCard

interface SearchAdvisersQuery {
    /**
     * Ejecuta la búsqueda de asesores basada en filtros.
     * Si todos los filtros son nulos, devuelve 32 al azar.
     */
    suspend fun execute(command: SearchCommand): List<AdviserCard>

    data class SearchCommand(
        val query: String?,       // Búsqueda por nombre
        val state: String?,      // Filtro por 'state_code'
        val level: String?,      // Filtro por 'level'
        val specialtyIds: List<Int>? // Filtro por IDs de materia
    )
}