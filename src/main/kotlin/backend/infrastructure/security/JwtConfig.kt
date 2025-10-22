package backend.infrastructure.security

data class JwtConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val realm: String,
    val expirationMinutes: Long
)