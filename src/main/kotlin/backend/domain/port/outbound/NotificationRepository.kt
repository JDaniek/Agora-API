package backend.domain.port.outbound

import backend.domain.model.Notification
import backend.domain.model.NotificationDetails

interface NotificationRepository {

    suspend fun createContactRequest(senderId: Long, recipientId: Long): Result<Long>

    /**
     * Ahora con filtro opcional por status.
     * Si 'statusFilter' es null o vacío → devuelve todas.
     */
    suspend fun findNotificationsForUser(
        userId: Long,
        statusFilter: List<String>? = null
    ): Result<List<NotificationDetails>>

    suspend fun updateStatus(
        notificationId: Long,
        newStatus: String,
        recipientId: Long
    ): Result<Boolean>

    suspend fun findById(notificationId: Long): Result<Notification?>

    suspend fun markAcceptedWithChat(
        notificationId: Long,
        recipientId: Long,
        chatId: Long
    ): Result<Boolean>
}
