package backend.infrastructure.outbound.persistence.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object MediaTable : Table("media") {
    val id = integer("id").autoIncrement()
    val url = text("url")
    val createdAt = timestamp("created_at").default(Instant.now())

    // Relación: Un archivo es subido por UN usuario
    val uploaderId = long("uploader_id").references(UserAccountsTable.id, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(id)
}