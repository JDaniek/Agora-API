package backend.infrastructure.inbound.http.mapper

import backend.domain.model.UserAccount
import backend.domain.port.inbound.LoginUseCase
import backend.domain.port.inbound.RegisterUserUseCase
import backend.infrastructure.inbound.http.dto.AuthResponse
import backend.infrastructure.inbound.http.dto.LoginRequest
import backend.infrastructure.inbound.http.dto.RegisterRequest

// DTO (Infra) -> Comando (App)
fun RegisterRequest.toCommand(): RegisterUserUseCase.RegisterUserCommand {
    return RegisterUserUseCase.RegisterUserCommand(
        firstName = this.firstName,
        secondName = this.secondName,
        lastName = this.lastName,
        roleId = this.roleId,
        email = this.email,
        password = this.password
    )
}

// DTO (Infra) -> Comando (App)
fun LoginRequest.toCommand(): LoginUseCase.LoginCommand {
    return LoginUseCase.LoginCommand(
        email = this.email,
        password = this.password
    )
}

// (Dominio, String) -> DTO (Infra)
fun Pair<UserAccount, String>.toAuthResponse(): AuthResponse {
    val user = this.first
    val token = this.second
    return AuthResponse(
        id = user.id,
        email = user.email,
        firstName = user.firstName,
        roleId = user.roleId,
        token = token
    )
}