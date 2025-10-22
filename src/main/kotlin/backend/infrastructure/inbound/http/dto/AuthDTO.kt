package backend.infrastructure.inbound.http.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val firstName: String,
    val secondName: String? = null,
    val lastName: String,
    val roleId: Long, // <--- DEBE SER Long
    val email: String,
    val password: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val id: Long, // <--- DEBE SER Long
    val email: String,
    val firstName: String,
    val roleId: Long, // <--- DEBE SER Long
    val token: String
)