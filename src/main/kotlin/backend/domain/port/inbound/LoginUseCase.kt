package backend.domain.port.inbound

import backend.domain.model.UserAccount

interface LoginUseCase {
    suspend fun execute(command: LoginCommand): Pair<UserAccount, String>

    data class LoginCommand(
        val email: String,
        val password: String
    )
}