package backend.domain.port.outbound

import backend.domain.model.NotificationDetails

/**
 * Puerto de salida (Repository Interface) para manejar la persistencia de las Notificaciones.
 * Define el "contrato" que la capa de dominio necesita.
 */
interface NotificationRepository {

    /**
     * Crea una nueva notificación de solicitud de contacto en la base de datos.
     *
     * @param senderId El ID del usuario que envía la solicitud (ej. "Ashwin").
     * @param recipientId El ID del usuario que recibe la solicitud (ej. "Alonso").
     * @return El ID de la nueva notificación creada, o un error.
     */
    suspend fun createContactRequest(senderId: Long, recipientId: Long): Result<Long>

    /**
     * Busca todas las notificaciones para un usuario específico (para la campana).
     * Ejecuta la consulta compleja con JOINs para obtener los detalles del remitente.
     *
     * @param userId El ID del usuario logueado (el 'recipient_id').
     * @return Una lista de [NotificationDetails] (el modelo de lectura) o un error.
     */
    suspend fun findNotificationsForUser(userId: Long): Result<List<NotificationDetails>>

    /**
     * Actualiza el estado de una notificación específica.
     * (Ej. de 'pending' a 'accepted' o 'declined').
     *
     * @param notificationId El ID de la notificación a actualizar.
     * @param newStatus El nuevo estado (ej. "accepted").
     * @param recipientId El ID del usuario que realiza la acción (para seguridad,
     * nos aseguramos que solo el receptor pueda actualizarla).
     * @return True si la actualización fue exitosa, o un error.
     */
    suspend fun updateStatus(notificationId: Long, newStatus: String, recipientId: Long): Result<Boolean>

    /**
     * (Opcional, pero útil) Encuentra una notificación por su ID para verificar
     * antes de crear un chat.
     *
     * @param notificationId El ID de la notificación.
     * @return El objeto [Notification] completo o null si no se encuentra.
     */
    // suspend fun findById(notificationId: Long): Result<Notification?>
}