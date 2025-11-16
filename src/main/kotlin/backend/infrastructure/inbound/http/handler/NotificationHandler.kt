package backend.infrastructure.inbound.http.handler

import backend.domain.port.inbound.AcceptContactRequestUseCase
import backend.domain.port.inbound.GetNotificationsQuery
import backend.infrastructure.inbound.http.dto.UpdateNotificationStatusRequest
import backend.infrastructure.inbound.http.dto.NewChatResponse
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.server.plugins.* // Para 'BadRequestException'

class NotificationHandler(
    private val getNotificationsQuery: GetNotificationsQuery,
    private val acceptContactRequestUseCase: AcceptContactRequestUseCase
) {

    /**
     * Handler para GET /api/notifications
     * Obtiene la lista de notificaciones para el usuario autenticado.
     */
    suspend fun getUserNotifications(call: ApplicationCall) {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.payload?.subject?.toLongOrNull()
            ?: throw IllegalStateException("No se encontró el ID de usuario en el token")

        val result = getNotificationsQuery.getNotifications(userId)

        result.onSuccess { notifications ->
            call.respond(HttpStatusCode.OK, notifications)
        }.onFailure { e ->
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
        }
    }

    /**
     * Handler para PATCH /api/notifications/{id}
     * Acepta o rechaza una solicitud de contacto.
     */
    suspend fun updateNotificationStatus(call: ApplicationCall) {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.payload?.subject?.toLongOrNull()
            ?: throw IllegalStateException("No se encontró el ID de usuario en el token")

        val notificationId = call.parameters["id"]?.toLongOrNull()
            ?: throw BadRequestException("El ID de la notificación debe ser un número.")

        val request = call.receive<UpdateNotificationStatusRequest>()

        if (request.status == "accepted") {
            // Lógica para ACEPTAR
            val result = acceptContactRequestUseCase.acceptRequest(notificationId, userId)

            result.onSuccess { newChatId ->
                call.respond(HttpStatusCode.Created, NewChatResponse(chatId = newChatId))
            }.onFailure { e ->
                // Manejar errores de negocio (ej. "ya fue aceptada")
                call.respond(HttpStatusCode.Conflict, mapOf("error" to e.message))
            }
        } else if (request.status == "declined") {
            // Lógica para RECHAZAR (¡Necesitaríamos un 'RejectContactRequestUseCase' simple!)
            // Por ahora, solo respondemos OK
            // TODO: Implementar 'rejectContactRequestUseCase.reject(notificationId, userId)'
            call.respond(HttpStatusCode.OK, mapOf("message" to "Solicitud rechazada"))
        } else {
            throw BadRequestException("El estado debe ser 'accepted' o 'declined'")
        }
    }
}