package backend.domain.port.inbound

/**
 * Caso de uso para aceptar una solicitud de contacto.
 * Orquesta la actualización de la notificación Y la creación del chat.
 */
interface AcceptContactRequestUseCase {
    /**
     * @param notificationId El ID de la notificación que se está aceptando.
     * @param acceptorUserId El ID del usuario que está aceptando (para seguridad).
     * @return Result con el ID del nuevo chat creado.
     */
    suspend fun acceptRequest(notificationId: Long, acceptorUserId: Long): Result<Long>
}