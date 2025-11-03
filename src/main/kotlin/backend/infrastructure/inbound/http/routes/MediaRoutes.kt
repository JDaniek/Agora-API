package backend.infrastructure.inbound.http.routes

import backend.infrastructure.inbound.http.handler.MediaHandler
import io.ktor.server.routing.*
import io.ktor.server.auth.*

fun Route.mediaRouting(handler: MediaHandler) {
    // Protegemos el endpoint de subida con JWT
    authenticate("auth-jwt") {
        route("/media") {
            // Endpoint: POST /api/v1/media/upload
            post("/upload") {// solo sube
                handler.handleFileUpload(call)
            }

            post("/upload-and-attach") { handler.handleUploadAndAttach(call) } // sube + persiste

        }
    }
}