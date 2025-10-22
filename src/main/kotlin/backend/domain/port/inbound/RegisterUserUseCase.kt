package backend.domain.port.inbound

import backend.domain.model.UserAccount

interface RegisterUserUseCase {
    // Asegúrate de que esta línea tenga 'suspend'
    suspend fun execute(command: RegisterUserCommand): UserAccount

    data class RegisterUserCommand(
        val firstName: String,
        val secondName: String?,
        val lastName: String,
        val roleId: Long, // <--- Asegúrate de que sea Long
        val email: String,
        val password: String
    )
}