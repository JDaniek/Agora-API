package backend.application.usecase.notifications

import backend.domain.model.NotificationDetails
import backend.domain.port.inbound.GetNotificationsQuery
import backend.domain.port.outbound.NotificationRepository

/**
 * Implementación del caso de uso (Query) [GetNotificationsQuery].
 * Depende del [NotificationRepository] para la persistencia.
 */
class GetNotificationsQueryImpl(
    private val notificationRepository: NotificationRepository
) : GetNotificationsQuery {

    override suspend fun getNotifications(userId: Long): Result<List<NotificationDetails>> {
        // Simplemente delega la llamada al repositorio.
        return notificationRepository.findNotificationsForUser(userId)
    }
}