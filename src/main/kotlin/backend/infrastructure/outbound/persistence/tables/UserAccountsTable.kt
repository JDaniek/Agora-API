package backend.infrastructure.outbound.persistence.tables

import backend.infrastructure.outbound.persistence.tables.RolesTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import java.time.OffsetDateTime
import java.time.ZoneOffset


object UserAccountsTable : Table("user_accounts") {
    val id = long("id").autoIncrement()

    val firstName = text("first_name")
    val secondName = text("second_name").nullable()
    val lastName = text("last_name")

    val roleId = long("role_id").references(RolesTable.id)

    val email = text("email").uniqueIndex()
    val passwordHash = text("password_hash")
    val isActive = bool("is_active").default(true)

    val createdAt = timestampWithTimeZone("created_at")
        .clientDefault { OffsetDateTime.now(ZoneOffset.UTC) }

    override val primaryKey = PrimaryKey(id)
}