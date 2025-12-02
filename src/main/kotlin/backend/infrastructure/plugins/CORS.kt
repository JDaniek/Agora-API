package backend.infrastructure.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureCORS() {
    install(CORS) {
        anyHost()

        // 2. Métodos HTTP permitidos
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)

        // 3. Cabeceras permitidas
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization) // Indispensable para el Token JWT

        // Permite que el navegador envíe credenciales/cookies si fuera necesario (opcional)
        allowCredentials = true
        allowNonSimpleContentTypes = true
    }
}