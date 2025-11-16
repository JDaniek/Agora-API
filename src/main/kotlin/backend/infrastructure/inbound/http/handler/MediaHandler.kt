package backend.infrastructure.inbound.http.handler

import backend.domain.port.outbound.ProfileRepository
import backend.domain.port.outbound.StorageService
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import java.io.ByteArrayOutputStream
import java.io.InputStream
import backend.infrastructure.inbound.http.dto.UploadAndAttachResponse

class MediaHandler(
    private val storageService: StorageService,
    private val profileRepository: ProfileRepository
) {

    suspend fun handleFileUpload(call: ApplicationCall) { /* ... */ }

    suspend fun handleUploadAndAttach(call: ApplicationCall) {
        // --- AÑADIMOS UN BLOQUE try/catch ---
        try {
            val principal = call.principal<JWTPrincipal>()
                ?: return call.respond(HttpStatusCode.Unauthorized, "No autorizado")

            val userIdLong = principal.payload.subject?.toLongOrNull()
                ?: principal.payload.getClaim("id").asLong()
                ?: principal.payload.getClaim("id").asInt()?.toLong()
                ?: return call.respond(HttpStatusCode.Unauthorized, "Token sin id de usuario")

            val multipart = call.receiveMultipart()
            var fileBytes: ByteArray? = null
            var originalFileName: String? = null

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> {
                        originalFileName = part.originalFileName ?: "avatar.jpg"
                        ByteArrayOutputStream().use { out ->
                            part.streamProvider().use { input: InputStream -> input.copyTo(out) }
                            fileBytes = out.toByteArray()
                        }
                    }
                    else -> {}
                }
                part.dispose()
            }

            if (fileBytes == null || originalFileName == null) {
                return call.respond(HttpStatusCode.BadRequest, "Falta 'file'")
            }

            val imageUrl = storageService.uploadFile(fileBytes!!, originalFileName!!)

            // Esta es la línea que puede fallar (la NPE)
            profileRepository.upsertPhoto(userIdLong, imageUrl)

            call.respond(HttpStatusCode.OK, UploadAndAttachResponse(photoUrl = imageUrl))

        } catch (e: Exception) {
            // Si algo falla (como la NPE), ahora lo manejamos
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al adjuntar foto: ${e.message}"))
        }
    }
}