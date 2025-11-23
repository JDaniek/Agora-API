package backend.application.usecase.notifications

import backend.domain.model.NotificationDetails
import backend.domain.port.inbound.GetNotificationsQuery
import backend.domain.port.outbound.NotificationRepository

class GetNotificationsQueryImpl(
    private val notificationRepository: NotificationRepository
) : GetNotificationsQuery {

    private val allowedStatuses = setOf(
        "pending",
        "read",
        "unread",
        "accepted",
        "declined",
        "archived"
    )

    override suspend fun getNotifications(
        userId: Long,
        status: String?
    ): Result<List<NotificationDetails>> {

        val normalizedStatus = status?.lowercase()

        if (normalizedStatus != null && normalizedStatus !in allowedStatuses) {
            return Result.failure(
                IllegalArgumentException("Estado de notificación inválido: $status")
            )
        }

        return notificationRepository.findNotificationsForUser(
            userId = userId,
            statusFilter = normalizedStatus?.let { listOf(it) } // 👈 CORRECCIÓN
        )
    }
}
