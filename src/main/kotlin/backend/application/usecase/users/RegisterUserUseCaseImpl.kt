package backend.application.usecase.users

import backend.domain.model.UserAccount
import backend.domain.port.inbound.RegisterUserUseCase
import backend.domain.port.outbound.PasswordService
import backend.domain.port.outbound.UserRepository

class RegisterUserUseCaseImpl(
    private val userRepository: UserRepository,
    private val passwordService: PasswordService
) : RegisterUserUseCase {

    override suspend fun execute(command: RegisterUserUseCase.RegisterUserCommand): UserAccount {
        // 1. Validar que el email no exista
        userRepository.findByEmail(command.email)?.let {
            throw IllegalArgumentException("El email ${command.email} ya está registrado.")
        }

        // 2. Crear el objeto de dominio
        val newUser = UserAccount(
            id = 0L, // 0L indica que es nuevo (L es de Long)
            firstName = command.firstName,
            secondName = command.secondName,
            lastName = command.lastName,
            roleId = command.roleId,
            email = command.email,
            passwordHash = passwordService.hashPassword(command.password),
            isActive = true, // Activo por defecto
            createdAt = null // Será generado por la BD
        )

        // 3. Guardar en el repositorio
        return userRepository.save(newUser)
    }
}