package backend.domain.model

import java.time.Instant
import kotlinx.serialization.Serializable

/**
 * Representa la fila de la base de datos 'notifications'.
 * Es el modelo de dominio principal.
 */
data class Notification(
    val id: Long,
    val recipientId: Long,
    val senderId: Long,
    val notificationTypeId: Int,
    val status: String,
    val createdAt: Instant,
    val message: String?,
    val chatId: Long?
)

/**
 * Representa el "Read Model" para el endpoint de la campana (la lista de notificaciones).
 * Es el resultado del JOIN con las tablas de usuarios y perfiles.
 *
 * NOTA: aquí usamos tipos amigables para JSON.
 */
@Serializable
data class NotificationDetails(
    val notificationId: Long,
    val status: String,
    val createdAt: String,          // ← String ISO, no Instant
    val notificationTypeName: String,
    val senderFirstName: String,
    val senderLastName: String,
    val senderPhotoUrl: String?
)
