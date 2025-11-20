package backend.application.usecase.notifications

import backend.domain.port.inbound.RejectContactRequestUseCase
import backend.domain.port.outbound.NotificationRepository

class RejectContactRequestUseCaseImpl(
    private val notificationRepository: NotificationRepository
) : RejectContactRequestUseCase {

    override suspend fun reject(notificationId: Long, recipientId: Long): Result<Unit> {
        return notificationRepository
            .updateStatus(notificationId, "declined", recipientId)
            .map { updated ->
                if (!updated) {
                    throw IllegalStateException(
                        "No se encontró la notificación o no pertenece al usuario autenticado."
                    )
                }
            }
    }
}
