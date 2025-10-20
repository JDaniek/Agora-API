package backend.infrastructure.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

data class JwtConfig(
    val issuer: String,
    val audience: String,
    val realm: String,
    val secret: String,
    val expiresInMinutes: Long
)

lateinit var jwtConfig: JwtConfig
lateinit var jwtAlgorithm: Algorithm

fun Application.configureSecurity() {
    val cfg = environment.config
    jwtConfig = JwtConfig(
        issuer = cfg.property("security.jwt.issuer").getString(),
        audience = cfg.property("security.jwt.audience").getString(),
        realm = cfg.property("security.jwt.realm").getString(),
        secret = cfg.property("security.jwt.secret").getString(),
        expiresInMinutes = cfg.property("security.jwt.expiresInMinutes").getString().toLong()
    )
    jwtAlgorithm = Algorithm.HMAC256(jwtConfig.secret)

    install(Authentication) {
        jwt("auth-jwt") {
            realm = jwtConfig.realm
            verifier(
                JWT
                    .require(jwtAlgorithm)
                    .withIssuer(jwtConfig.issuer)
                    .withAudience(jwtConfig.audience)
                    .build()
            )
            validate { cred ->
                if (cred.payload.getClaim("uid").asLong() != null) JWTPrincipal(cred.payload) else null
            }
        }
    }
}

fun issueToken(userId: Long, roleName: String): String {
    val now = System.currentTimeMillis()
    val exp = now + jwtConfig.expiresInMinutes * 60_000
    return JWT.create()
        .withIssuer(jwtConfig.issuer)
        .withAudience(jwtConfig.audience)
        .withClaim("uid", userId)
        .withClaim("role", roleName)
        .withIssuedAt(java.util.Date(now))
        .withExpiresAt(java.util.Date(exp))
        .sign(jwtAlgorithm)
}
