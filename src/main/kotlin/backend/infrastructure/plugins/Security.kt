package backend.infrastructure.plugins

import backend.infrastructure.security.JwtConfig
import backend.infrastructure.security.JwtService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.http.auth.* //IMPORTANTE: Necesario para HttpAuthHeader y parseAuthorizationHeader
import org.koin.ktor.ext.inject

fun Application.configureSecurity() {
    // Inyecta los servicios que Koin ya creó en 'configureDependencyInjection'
    val jwtService by inject<JwtService>()
    val cfg by inject<JwtConfig>()

    install(Authentication) {
        jwt("auth-jwt") {
            realm = cfg.realm
            verifier(jwtService.verifier())

            // LÓGICA PARA LEER TOKEN DE LA URL (WEBSOCKETS)
            authHeader { call ->
                // 1. Primero intentamos leer el Header estándar (Authorization: Bearer ...)
                // Esto es lo que usan tus rutas normales (REST)
                val authHeader = call.request.parseAuthorizationHeader()
                if (authHeader != null) {
                    return@authHeader authHeader
                }

                // 2. Si no hay header, intentamos leer el Query Param "?token="
                // Esto es lo que usaremos para el WebSocket
                val token = call.request.queryParameters["token"]
                if (token != null) {
                    // "Engañamos" a Ktor empaquetando el token como si fuera un Bearer header
                    return@authHeader HttpAuthHeader.Single("Bearer", token)
                }

                // 3. Si no hay ni header ni param, retornamos null (Ktor lanzará 401)
                null
            }
            //

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