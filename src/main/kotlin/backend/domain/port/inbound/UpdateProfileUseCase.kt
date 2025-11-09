package backend.domain.port.inbound

import backend.domain.model.Profile

// Puerto de entrada (lo que la aplicación puede hacer)
interface UpdateProfileUseCase {
    suspend fun execute(command: UpdateProfileCommand): Profile

    data class UpdateProfileCommand(
        val userId: Long,
        val description: String?,   // ← antes String
        val photoUrl: String?,      // ← antes String
        val city: String?,          // ← antes String
        val stateCode: String,
        val level: String,
        val specialtyIds: List<Long>
    )
}

