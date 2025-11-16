package backend.infrastructure.outbound.persistence.tables

import org.jetbrains.exposed.sql.Table
// --- IMPORTS AÑADIDOS ---
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.sql.javatime.CurrentTimestampWithTimeZone

object ChatsTable : Table("chats") {
    val id = long("id").autoIncrement()
    val chatName = varchar("chat_name", 200).nullable()

    // --- CORREGIDO: Estandarizado a 'timestamptz' ---
    val createdAt = timestampWithTimeZone("created_at")
        .defaultExpression(CurrentTimestampWithTimeZone)
    // ----------------------------------------------

    override val primaryKey = PrimaryKey(id)
}