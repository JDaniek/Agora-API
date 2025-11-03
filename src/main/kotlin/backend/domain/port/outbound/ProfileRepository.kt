package backend.domain.port.outbound

import backend.domain.model.Profile

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

    suspend fun upsertPhoto(userId: Long, photoUrl: String): Profile

}
