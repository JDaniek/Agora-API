package org.agora.backend.infrastructure.outbound.persistance.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant
import backend.infrastructure.outbound.persistence.tables.UserAccountsTable

object ChatMessagesTable : Table("chat_messages") {
    val messageId = long("message_id").autoIncrement()
    val chatId = long("chat_id").references(ChatsTable.id, onDelete = ReferenceOption.CASCADE)
    val senderId = long("sender_id").references(UserAccountsTable.id, onDelete = ReferenceOption.CASCADE)
    val body = text("body")
    val sentAt = timestamp("sent_at").clientDefault{(Instant.now())}
    override val primaryKey = PrimaryKey(messageId)
}