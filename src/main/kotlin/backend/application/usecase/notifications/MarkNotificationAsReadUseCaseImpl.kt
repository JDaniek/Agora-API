package backend.application.usecase.notifications

import backend.domain.port.inbound.MarkNotificationAsReadUseCase
import backend.domain.port.outbound.NotificationRepository

class MarkNotificationAsReadUseCaseImpl(
    private val notificationRepository: NotificationRepository
) : MarkNotificationAsReadUseCase {

    override suspend fun markAsRead(notificationId: Long, userId: Long): Result<Unit> {
        return notificationRepository
            .updateStatus(notificationId, "read", userId)
            .map { updated ->
                if (!updated) {
                    throw IllegalStateException(
                        "No se encontró la notificación o no pertenece al usuario autenticado."
                    )
                }
            }
    }
}
