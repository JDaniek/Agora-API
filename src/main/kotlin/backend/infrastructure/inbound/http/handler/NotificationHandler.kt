package backend.infrastructure.inbound.http.handler

import backend.domain.port.inbound.AcceptContactRequestUseCase
import backend.domain.port.inbound.GetNotificationsQuery
import backend.domain.port.inbound.MarkNotificationAsReadUseCase
import backend.infrastructure.inbound.http.dto.UpdateNotificationStatusRequest
import backend.infrastructure.inbound.http.dto.NewChatResponse
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.server.plugins.* // Para 'BadRequestException'
import backend.domain.port.inbound.RejectContactRequestUseCase  // <--- NUEVO

class NotificationHandler(
    private val getNotificationsQuery: GetNotificationsQuery,
    private val acceptContactRequestUseCase: AcceptContactRequestUseCase,
    private val rejectContactRequestUseCase: RejectContactRequestUseCase,
    private val markNotificationAsReadUseCase: MarkNotificationAsReadUseCase // <--- NUEVO
) {

    /**
     * Handler para GET /api/notifications
     * Obtiene la lista de notificaciones para el usuario autenticado.
     */
    suspend fun getUserNotifications(call: ApplicationCall) {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.payload?.subject?.toLongOrNull()
            ?: throw IllegalStateException("No se encontró el ID de usuario en el token")

        // ?status=pending / accepted / declined / read / unread / archived
        val statusParam = call.request.queryParameters["status"]

        val result = getNotificationsQuery.getNotifications(userId, statusParam)

        result.onSuccess { notifications ->
            call.respond(HttpStatusCode.OK, notifications)
        }.onFailure { e ->
            val statusCode =
                if (e is IllegalArgumentException) HttpStatusCode.BadRequest
                else HttpStatusCode.InternalServerError

            call.respond(statusCode, mapOf("error" to (e.message ?: "Error inesperado")))
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

        when (request.status) {
            "accepted" -> {
                val result = acceptContactRequestUseCase.acceptRequest(notificationId, userId)

                result.onSuccess { newChatId ->
                    call.respond(HttpStatusCode.Created, NewChatResponse(chatId = newChatId))
                }.onFailure { e ->
                    call.respond(HttpStatusCode.Conflict, mapOf("error" to e.message))
                }
            }

            "declined" -> {
                val result = rejectContactRequestUseCase.reject(notificationId, userId)

                result.onSuccess {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Solicitud rechazada"))
                }.onFailure { e ->
                    call.respond(HttpStatusCode.Conflict, mapOf("error" to e.message))
                }
            }

            "read" -> {
                val result = markNotificationAsReadUseCase.markAsRead(notificationId, userId)

                result.onSuccess {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Notificación marcada como leída"))
                }.onFailure { e ->
                    call.respond(HttpStatusCode.Conflict, mapOf("error" to e.message))
                }
            }

            else -> {
                throw BadRequestException("El estado debe ser 'accepted', 'declined' o 'read'")
            }
        }
    }
}