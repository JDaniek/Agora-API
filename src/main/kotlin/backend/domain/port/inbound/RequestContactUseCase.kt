package backend.domain.port.inbound

/**
 * Caso de uso para crear una solicitud de contacto (una notificación).
 */
interface RequestContactUseCase {
    /**
     * @param senderId El ID del usuario que envía la solicitud (autenticado).
     * @param recipientId El ID del usuario que recibe la solicitud (el perfil del asesor).
     * @return Result con el ID de la nueva notificación.
     */
    suspend fun requestContact(senderId: Long, recipientId: Long): Result<Long>
}