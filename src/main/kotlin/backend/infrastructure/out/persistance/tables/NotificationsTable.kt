package org.agora.backend.infrastructure.out.persistance.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object NotificationsTable : Table("notifications") {
    val id = integer("id").autoIncrement()
    val message = text("message")

    // Relación: Una notificación es para UN usuario
    val userId = long("user_id").references(UserAccountsTable.id, onDelete = ReferenceOption.CASCADE)

    // Relación: Una notificación tiene UN tipo
    val notificationTypeId = integer("notification_type_id").references(NotificationTypesTable.id)

    // Relación (opcional): Una notificación puede estar ligada a UN chat
    val chatId = long("chat_id").references(ChatsTable.id, onDelete = ReferenceOption.CASCADE).nullable()

    override val primaryKey = PrimaryKey(id)
}