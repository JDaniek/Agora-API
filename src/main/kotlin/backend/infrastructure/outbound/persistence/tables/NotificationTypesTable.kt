package backend.infrastructure.outbound.persistence.tables
import org.jetbrains.exposed.sql.Table

object NotificationTypesTable : Table("notification_type_id") {
    val id = integer("id").autoIncrement()
    val name = text("name")
    override val primaryKey = PrimaryKey(id)
}