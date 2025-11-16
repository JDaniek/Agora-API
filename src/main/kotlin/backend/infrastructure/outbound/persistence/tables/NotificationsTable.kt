package backend.infrastructure.outbound.persistence.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentTimestampWithTimeZone
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object NotificationsTable : Table("notifications") {
    // ID principal, cambiado a long() para coincidir con BIGSERIAL
    val id = long("id").autoIncrement()

    // --- Columnas de la nueva lógica ---

    // El usuario que RECIBE la notificación (antes 'userId')
    val recipientId = long("recipient_id").references(UserAccountsTable.id, onDelete = ReferenceOption.CASCADE)

    // El usuario que ENVÍA la notificación (ej. "Ashwin Bose")
    val senderId = long("sender_id").references(UserAccountsTable.id, onDelete = ReferenceOption.CASCADE)

    // El estado de la notificación (pending, read, accepted, declined)
    val status = varchar("status", 20).default("pending")
    // Usamos CurrentTimestampWithTimeZone (tipo OffsetDateTime) para que coincida
    // con la columna timestampWithTimeZone.
    val createdAt = timestampWithTimeZone("created_at").defaultExpression(CurrentTimestampWithTimeZone)

    // --- Columnas originales ---

    // Mensaje opcional (para notificaciones genéricas)
    val message = text("message").nullable()

    // Relación: Una notificación tiene UN tipo
    val notificationTypeId = integer("notification_type_id").references(NotificationTypesTable.id)

    // Relación (opcional): Una notificación puede estar ligada a UN chat
    val chatId = long("chat_id").references(ChatsTable.id, onDelete = ReferenceOption.CASCADE).nullable()

    override val primaryKey = PrimaryKey(id)
}