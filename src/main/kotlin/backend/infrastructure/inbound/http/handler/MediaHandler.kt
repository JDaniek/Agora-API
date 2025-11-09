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
    private val profileRepository: ProfileRepository  // 👈 NUEVO
) {
    // Tu método existente (solo subir) se queda igual:
    suspend fun handleFileUpload(call: ApplicationCall) { /* ... */
    }

    // NUEVO: sube y persiste en profiles.photo_url del usuario autenticado
    suspend fun handleUploadAndAttach(call: ApplicationCall) {
        val principal = call.principal<JWTPrincipal>()
            ?: return call.respond(HttpStatusCode.Unauthorized, "No autorizado")

        // Ajusta según tu claim real
        // 1) Primero intenta del subject (tu JwtService guarda ahí el userId)
        val userIdLong = principal.payload.subject?.toLongOrNull()
        // 2) Si algún día decides meter "id" como claim, esto lo vuelve compatible
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
        profileRepository.upsertPhoto(userIdLong, imageUrl)

        call.respond(HttpStatusCode.OK, UploadAndAttachResponse(photoUrl = imageUrl))
    }
}
