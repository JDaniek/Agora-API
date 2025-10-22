package backend.infrastructure.security

import backend.domain.model.UserAccount
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.JWTVerifier // <-- Importa esto
import java.util.Date

class JwtService(private val config: JwtConfig) {

    private val algorithm = Algorithm.HMAC256(config.secret)

    /**
     * Crea un verificador de JWT para que Ktor lo use.
     */
    fun verifier(): JWTVerifier {
        return JWT.require(algorithm)
            .withIssuer(config.issuer)
            .withAudience(config.audience)
            .build()
    }

    /**
     * Genera un nuevo token JWT para un usuario.
     */
    fun generateToken(user: UserAccount): String {
        val now = System.currentTimeMillis()
        val expiresAt = Date(now + config.expirationMinutes * 60_000)

        return JWT.create()
            .withIssuer(config.issuer)
            .withAudience(config.audience)
            .withSubject(user.id.toString()) // <-- Usamos 'subject' para el ID
            .withIssuedAt(Date(now))
            .withExpiresAt(expiresAt)
            // Claims personalizados
            .withClaim("email", user.email)
            .withClaim("role", user.roleId)
            .sign(algorithm)
    }
}