package backend.domain.port.inbound

import backend.domain.model.Profile

// Puerto de entrada (lo que la aplicación puede hacer)
interface UpdateProfileUseCase {
    suspend fun execute(command: UpdateProfileCommand): Profile

    data class UpdateProfileCommand(
        val userId: Long, // El ID del usuario (lo sacaremos del JWT)
        val description: String,
        val photoUrl: String,
        val city: String,
        val stateCode: String,
        val level: String,
        val specialtyIds: List<Long>
    )
}
