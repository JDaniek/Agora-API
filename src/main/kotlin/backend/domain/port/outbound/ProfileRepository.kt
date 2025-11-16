<<<<<<< HEAD
=======
package backend.domain.port.outbound

import backend.domain.model.AdviserCard // <-- 1. IMPORTAR
import backend.domain.model.Profile
import backend.domain.port.inbound.SearchAdvisersQuery // <-- 2. IMPORTAR

interface ProfileRepository {
    /**
     * Guarda o actualiza un perfil. También actualiza las especialidades
     * en la tabla 'user_specialties'.
     */
    suspend fun saveOrUpdate(profile: Profile, specialtyIds: List<Long>): Profile

    /**
     * Busca un perfil por el ID de usuario.
     */
    suspend fun findByUserId(userId: Long): Profile?

    suspend fun upsertPhoto(userId: Long, photoUrl: String): Unit

    /**
     * Busca perfiles de asesores (role_id=2) que coincidan
     * con los filtros de búsqueda.
     */
    suspend fun searchAdvisers(command: SearchAdvisersQuery.SearchCommand): List<AdviserCard>

}
>>>>>>> develop
