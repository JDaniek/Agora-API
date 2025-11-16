package backend.infrastructure.outbound.persistence.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
// --- IMPORTS AÑADIDOS ---
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.sql.javatime.CurrentTimestampWithTimeZone

object ChatMessagesTable : Table("chat_messages") {
    val messageId = long("message_id").autoIncrement()
    val chatId = long("chat_id").references(ChatsTable.id, onDelete = ReferenceOption.CASCADE)
    val senderId = long("sender_id").references(UserAccountsTable.id, onDelete = ReferenceOption.CASCADE)
    val body = text("body")

    // --- CORREGIDO: Estandarizado a 'timestamptz' ---
    val sentAt = timestampWithTimeZone("sent_at")
        .defaultExpression(CurrentTimestampWithTimeZone)
    // ----------------------------------------------

    override val primaryKey = PrimaryKey(messageId)
}