package backend.infrastructure.inbound.http.handler

import backend.domain.port.inbound.LoginUseCase
import backend.domain.port.inbound.RegisterUserUseCase
import backend.infrastructure.inbound.http.dto.LoginRequest
import backend.infrastructure.inbound.http.dto.RegisterRequest
import backend.infrastructure.inbound.http.mapper.toAuthResponse
import backend.infrastructure.inbound.http.mapper.toCommand
import backend.infrastructure.security.JwtService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*

class AuthHandler(
    private val registerUserUseCase: RegisterUserUseCase,
    private val loginUseCase: LoginUseCase,
    private val jwtService: JwtService // Lo necesitamos para el token en registro
) {

    suspend fun handleRegister(call: ApplicationCall) {
        val request = call.receive<RegisterRequest>()
        val command = request.toCommand()

        // El caso de uso registra al usuario
        val newUser = registerUserUseCase.execute(command)

        // Generamos un token para él inmediatamente
        val token = jwtService.generateToken(newUser)

        // Respondemos con el DTO
        call.respond(
            HttpStatusCode.Created,
            Pair(newUser, token).toAuthResponse()
        )
    }

    suspend fun handleLogin(call: ApplicationCall) {
        val request = call.receive<LoginRequest>()
        val command = request.toCommand()

        // El caso de uso nos da el Usuario y el Token
        val (user, token) = loginUseCase.execute(command)

        // Respondemos con el DTO
        call.respond(
            HttpStatusCode.OK,
            Pair(user, token).toAuthResponse()
        )
    }
}