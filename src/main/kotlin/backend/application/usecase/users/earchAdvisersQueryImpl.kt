package backend.application.usecase.users

import backend.domain.model.AdviserCard
import backend.domain.port.inbound.SearchAdvisersQuery
import backend.domain.port.outbound.ProfileRepository

class SearchAdvisersQueryImpl(
    private val profileRepository: ProfileRepository
) : SearchAdvisersQuery {

    override suspend fun execute(
        command: SearchAdvisersQuery.SearchCommand
    ): List<AdviserCard> {
        // La lógica compleja vive en el repositorio
        return profileRepository.searchAdvisers(command)
    }
}