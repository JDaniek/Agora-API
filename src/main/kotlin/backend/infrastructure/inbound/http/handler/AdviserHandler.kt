package backend.infrastructure.inbound.http.handler

import backend.domain.port.inbound.RequestContactUseCase // <-- ¡NUEVO IMPORT!
import backend.domain.port.inbound.SearchAdvisersQuery
import backend.infrastructure.inbound.http.dto.toDTO
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.server.auth.* // <-- ¡NUEVO IMPORT!
import io.ktor.server.auth.jwt.* // <-- ¡NUEVO IMPORT!
import io.ktor.server.plugins.* // <-- ¡NUEVO IMPORT!
import java.lang.IllegalStateException // <-- ¡NUEVO IMPORT!

class AdviserHandler(
    // Dependencias existentes
    private val searchAdvisersQuery: SearchAdvisersQuery,
    // --- ¡NUEVA DEPENDENCIA! ---
    private val requestContactUseCase: RequestContactUseCase
) {

    /**
     * Handler para GET /api/advisers
     * (Este es tu método original, está perfecto)
     */
    suspend fun handleSearchAdvisers(call: ApplicationCall) {
        try {
            val params = call.request.queryParameters
            val query = params["q"]
            val state = params["state"]
            val level = params["level"]
            val specialtyIds = params.getAll("specialty")?.mapNotNull { it.toIntOrNull() }

            val command = SearchAdvisersQuery.SearchCommand(
                query = query,
                state = state,
                level = level,
                specialtyIds = specialtyIds
            )

            val adviserCards = searchAdvisersQuery.execute(command)

            call.respond(HttpStatusCode.OK, adviserCards.map { it.toDTO() })

        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Error al buscar asesores: ${e.message}")
        }
    }

    // --- ¡NUEVO MÉTODO! ---
    /**
     * Handler para POST /api/advisers/{id}/contact
     * Crea una solicitud de contacto para un asesor.
     */
    suspend fun requestContact(call: ApplicationCall) {
        try {
            // 1. Obtener el Sender ID (del usuario autenticado)
            val principal = call.principal<JWTPrincipal>()
            val senderId = principal?.payload?.subject?.toLongOrNull()
                ?: throw IllegalStateException("No se encontró el ID de usuario en el token")

            // 2. Obtener el Recipient ID (del asesor en la URL)
            val recipientId = call.parameters["id"]?.toLongOrNull()
                ?: throw BadRequestException("El ID del asesor debe ser un número.")

            // 3. Validación de negocio
            if (senderId == recipientId) {
                throw BadRequestException("No puedes contactarte a ti mismo.")
            }

            // 4. Llamar al caso de uso
            val result = requestContactUseCase.requestContact(senderId, recipientId)

            // 5. Responder
            result.onSuccess { newNotificationId ->
                call.respond(HttpStatusCode.Created, mapOf("notificationId" to newNotificationId))
            }.onFailure { e ->
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        } catch (e: BadRequestException) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
        } catch (e: IllegalStateException) {
            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to e.message))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
        }
    }
}