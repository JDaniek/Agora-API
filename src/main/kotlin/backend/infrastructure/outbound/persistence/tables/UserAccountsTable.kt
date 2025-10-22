package backend.infrastructure.outbound.persistence.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import java.time.OffsetDateTime
import java.time.ZoneOffset

object RolesTable : Table("roles") {
    val id = long("id").autoIncrement()
    val name = text("name").uniqueIndex()
    override val primaryKey = PrimaryKey(id)
}

object UserAccountsTable : Table("user_accounts") {
    val id = long("id").autoIncrement()

    val firstName = text("first_name")
    val secondName = text("second_name").nullable()
    val lastName = text("last_name")

    val roleId = long("role_id").references(RolesTable.id)

    val email = text("email").uniqueIndex()
    val passwordHash = text("password_hash")
    val isActive = bool("is_active").default(true)

    // ✅ Sin CustomFunction. Usa la hora del lado de la app (UTC).
    val createdAt = timestampWithTimeZone("created_at")
        .clientDefault { OffsetDateTime.now(ZoneOffset.UTC) }

    override val primaryKey = PrimaryKey(id)
}
