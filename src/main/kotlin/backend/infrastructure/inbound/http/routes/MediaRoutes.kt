package backend.infrastructure.inbound.http.routes

import backend.infrastructure.inbound.http.handler.MediaHandler
import io.ktor.server.routing.*
import io.ktor.server.auth.*
import io.ktor.server.application.* // <-- Import 'call' (implícito)

fun Route.mediaRouting(handler: MediaHandler) {
    authenticate("auth-jwt") {
        route("/media") {
            post("/upload") {
                handler.handleFileUpload(call)
            }
            post("/upload-and-attach") {
                handler.handleUploadAndAttach(call)
            }
        }
    }
}