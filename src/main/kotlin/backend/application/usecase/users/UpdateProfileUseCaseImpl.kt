package backend.application.usecase.users

import backend.domain.model.Profile
import backend.domain.port.inbound.UpdateProfileUseCase
import backend.domain.port.outbound.ProfileRepository

class UpdateProfileUseCaseImpl(
    private val profileRepository: ProfileRepository
) : UpdateProfileUseCase {

    override suspend fun execute(command: UpdateProfileUseCase.UpdateProfileCommand): Profile {
        // 1. Convierte el "comando" en un objeto de dominio "Profile"
        val profile = Profile(
            userId = command.userId,
            description = command.description,
            photoUrl = command.photoUrl,
            city = command.city,
            stateCode = command.stateCode,
            level = command.level
        )

        // 2. Llama al repositorio para que haga el trabajo sucio de guardado
        return profileRepository.saveOrUpdate(profile, command.specialtyIds)
    }
}
