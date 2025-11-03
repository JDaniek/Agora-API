package backend.infrastructure.plugins // ✅

import backend.infrastructure.security.JwtConfig
import backend.infrastructure.security.JwtService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import org.koin.ktor.ext.inject // <-- Importa el inyector de Koin

fun Application.configureSecurity() {
    // Inyecta los servicios que Koin ya creó en 'configureDependencyInjection'
    val jwtService by inject<JwtService>()
    val cfg by inject<JwtConfig>()

    install(Authentication) {
        jwt("auth-jwt") {
            realm = cfg.realm
            verifier(jwtService.verifier()) // <-- Ahora esto funciona
            validate { credential ->
                // Leemos el ID desde el campo 'subject' (sub) del token
                val userId = credential.payload.subject?.toLongOrNull()

                if (userId != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null // Token inválido si no tiene subject
                }
            }
        }
    }
}