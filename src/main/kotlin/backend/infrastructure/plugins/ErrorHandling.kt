package backend.infrastructure.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable

@Serializable
data class Problem(
    val type: String = "about:blank",
    val title: String,
    val status: Int,
    val detail: String? = null
)

fun Application.configureErrorHandling() {
    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = Problem(
                    title = "Bad Request",
                    status = HttpStatusCode.BadRequest.value,
                    detail = cause.message
                )
            )
        }
        exception<Throwable> { call, cause ->
            call.respond(
                status = HttpStatusCode.InternalServerError,
                message = Problem(
                    title = "Internal Server Error",
                    status = HttpStatusCode.InternalServerError.value,
                    detail = cause.message
                )
            )
        }
    }
}
