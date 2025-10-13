package org.agora.backend.infrastructure.out.persistance.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object UserAccountsTable : Table("user_accounts") {
    val id = long("id").autoIncrement()
    val firstName = text("first_name")
    val secondName = text("second_name").nullable()
    val lastName = text("last_name")
    val email = text("email").uniqueIndex()
    val passwordHash = text("password_hash")
    val isActive = bool("is_active").default(true)
    val createdAt = timestamp("created_at").default(Instant.now())
    val roleId = integer("role_id").references(RolesTable.id)
    override val primaryKey = PrimaryKey(id)
}