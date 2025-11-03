package backend.application.usecase.users

import backend.domain.port.inbound.GetProfileQuery
import backend.domain.port.inbound.ProfileDetails
import backend.domain.port.outbound.ProfileRepository
import backend.domain.port.outbound.SpecialtyRepository

class GetProfileQueryImpl(
    private val profileRepository: ProfileRepository,
    private val specialtyRepository: SpecialtyRepository
) : GetProfileQuery {

    override suspend fun execute(userId: Long): ProfileDetails {
        // 1. Busca el perfil en la tabla 'profiles'
        val profile = profileRepository.findByUserId(userId)

        // 2. Busca las especialidades en 'user_specialties'
        val specialties = specialtyRepository.findByUserId(userId)

        // 3. Devuelve los datos combinados
        return ProfileDetails(profile, specialties)
    }
}
