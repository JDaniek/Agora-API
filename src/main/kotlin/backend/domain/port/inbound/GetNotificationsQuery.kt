package backend.domain.port.inbound

import backend.domain.model.NotificationDetails

/**
 * Caso de uso (Query) para obtener la lista de notificaciones de un usuario.
 */
interface GetNotificationsQuery {
    /**
     * @param userId El ID del usuario autenticado (el que mira la campana).
     * @return Result con la lista de [NotificationDetails].
     */
    suspend fun getNotifications(userId: Long): Result<List<NotificationDetails>>
}