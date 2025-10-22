package backend.domain.port.inbound

import backend.domain.model.UserAccount

interface LoginUseCase {
    // Asegúrate de que esta línea tenga 'suspend'
    suspend fun execute(command: LoginCommand): Pair<UserAccount, String>

    data class LoginCommand(
        val email: String,
        val password: String
    )
}