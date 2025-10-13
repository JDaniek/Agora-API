package org.agora.backend.infrastructure.out.persistance.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object ChatMembersTable : Table("chat_members") {
    val chatId = long("chat_id").references(ChatsTable.id, onDelete = ReferenceOption.CASCADE)
    val userId = long("user_id").references(UserAccountsTable.id, onDelete = ReferenceOption.CASCADE)
    override val primaryKey = PrimaryKey(chatId, userId)
}