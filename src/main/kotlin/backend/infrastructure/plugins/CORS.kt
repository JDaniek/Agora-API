package backend.infrastructure.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

// Esta es la función que llamaremos desde Application.kt
fun Application.configureCORS() {
    install(CORS) {
        // 1. Permite peticiones desde tu frontend Angular
        allowHost("localhost:4200")

        // 2. Métodos HTTP que permites desde el frontend
        allowMethod(HttpMethod.Options) // ¡Crítico para el "preflight"!
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)

        // 3. Cabeceras (headers) que permites que el frontend envíe
        allowHeader(HttpHeaders.ContentType)     // Para que Angular pueda enviar JSON
        allowHeader(HttpHeaders.Authorization) // Para que pueda enviar el Token JWT

        // Opcional: si necesitas que el frontend lea otras cabeceras,
        // puedes exponerlas aquí, pero no es necesario para el login.
        // exposeHeader("X-Mi-Header-Custom")
    }
}