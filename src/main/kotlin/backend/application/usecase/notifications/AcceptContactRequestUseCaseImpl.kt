package backend.application.usecase.notifications

import backend.domain.port.inbound.AcceptContactRequestUseCase
import backend.domain.port.outbound.ChatRepository
import backend.domain.port.outbound.NotificationRepository

class AcceptContactRequestUseCaseImpl(
    private val notificationRepository: NotificationRepository,
    private val chatRepository: ChatRepository
) : AcceptContactRequestUseCase {

    override suspend fun acceptRequest(notificationId: Long, acceptorUserId: Long): Result<Long> {
        // 1. Buscar la notificación
        val notification = notificationRepository.findById(notificationId)
            .getOrElse { return Result.failure(it) }
            ?: return Result.failure(Exception("Notificación no encontrada"))

        // 2. Validaciones de negocio
        if (notification.recipientId != acceptorUserId) {
            return Result.failure(Exception("No autorizado. Esta notificación no es tuya."))
        }

        if (notification.status != "pending") {
            return Result.failure(Exception("Esta solicitud ya fue ${notification.status}."))
        }

        return try {
            // 3. Buscar si ya existe un chat entre estos 2 usuarios
            val existingChatId = chatRepository
                .findPrivateChatBetweenUsers(notification.senderId, notification.recipientId)
                .getOrThrow()

            val chatId = if (existingChatId != null) {
                existingChatId
            } else {
                chatRepository.createPrivateChat(
                    userOneId = notification.senderId,
                    userTwoId = notification.recipientId
                ).getOrThrow()
            }

            // 4. Marcar la notificación como aceptada y asociar el chat
            val updated = notificationRepository.markAcceptedWithChat(
                notificationId = notificationId,
                recipientId = acceptorUserId,
                chatId = chatId
            ).getOrThrow()

            if (!updated) {
                throw IllegalStateException("No se pudo actualizar la notificación como aceptada.")
            }

            // 5. Devolver el ID del chat (ya sea nuevo o existente)
            Result.success(chatId)

        } catch (e: Exception) {
            Result.failure(Exception("Error al aceptar la solicitud: ${e.message}"))
        }
    }
}
