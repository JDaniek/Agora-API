package backend.plugins // (Asumo que está en 'plugins' por el package)

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureErrorHandling() {
    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to cause.message))
        }
        exception<SecurityException> { call, cause -> // <-- Añadido para el login
            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to cause.message))
        }
        exception<IllegalStateException> { call, cause ->
            call.respond(HttpStatusCode.Forbidden, mapOf("error" to cause.message))
        }
        exception<Throwable> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (cause.message ?: "server error")))
        }
    }
}