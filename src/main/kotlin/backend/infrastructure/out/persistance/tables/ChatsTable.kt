package org.agora.backend.infrastructure.out.persistance.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object ChatsTable : Table("chats") {
    val id = long("id").autoIncrement()
    val chatName = varchar("chat_name", 200).nullable()
    val createdAt = timestamp("created_at").clientDefault{(Instant.now())}
    override val primaryKey = PrimaryKey(id)
}