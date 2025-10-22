package backend.application.usecase.users

import backend.domain.model.UserAccount
import backend.domain.port.inbound.LoginUseCase
import backend.domain.port.outbound.PasswordService
import backend.domain.port.outbound.UserRepository
import backend.infrastructure.security.JwtService // Importamos el servicio de infra

class LoginUseCaseImpl(
    private val userRepository: UserRepository,
    private val passwordService: PasswordService,
    private val jwtService: JwtService // Inyectamos el servicio JWT
) : LoginUseCase {

    override suspend fun execute(command: LoginUseCase.LoginCommand): Pair<UserAccount, String> {
        // 1. Buscar usuario por email
        val user = userRepository.findByEmail(command.email)
            ?: throw SecurityException("Credenciales inválidas.") // No dar pistas

        // 2. Verificar contraseña
        if (!passwordService.verifyPassword(command.password, user.passwordHash)) {
            throw SecurityException("Credenciales inválidas.")
        }

        // 3. Verificar si está activo
        if (!user.isActive) {
            throw SecurityException("La cuenta está desactivada.")
        }

        // 4. Generar token
        val token = jwtService.generateToken(user)

        // 5. Retornar el par
        return Pair(user, token)
    }
}