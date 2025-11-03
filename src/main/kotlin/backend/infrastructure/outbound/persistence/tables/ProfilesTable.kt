package backend.infrastructure.outbound.persistence.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object ProfilesTable : Table("profiles") {
    val userId = long("user_id")
        .uniqueIndex()
        .references(UserAccountsTable.id, onDelete = ReferenceOption.CASCADE)

    val description = text("description").nullable()
    val photoUrl = text("photo_url").nullable()
    val city = text("city").nullable()
    val stateCode = text("state_code").nullable()
    val level = text("level").nullable()

    override val primaryKey = PrimaryKey(userId)
}
