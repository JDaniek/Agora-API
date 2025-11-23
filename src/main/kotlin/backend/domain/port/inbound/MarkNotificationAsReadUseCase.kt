package backend.domain.port.inbound

/**
 * Caso de uso para marcar una notificación como leída.
 */
interface MarkNotificationAsReadUseCase {
    /**
     * @param notificationId ID de la notificación a marcar.
     * @param userId ID del usuario autenticado (debe ser el recipient).
     */
    suspend fun markAsRead(notificationId: Long, userId: Long): Result<Unit>
}
