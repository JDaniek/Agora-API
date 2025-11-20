package backend.domain.port.inbound

/**
 * Caso de uso para rechazar una solicitud de contacto.
 */
interface RejectContactRequestUseCase {
    /**
     * @param notificationId ID de la notificación a rechazar.
     * @param recipientId ID del usuario autenticado que está rechazando.
     */
    suspend fun reject(notificationId: Long, recipientId: Long): Result<Unit>
}
