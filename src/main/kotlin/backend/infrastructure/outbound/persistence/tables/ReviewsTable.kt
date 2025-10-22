package backend.infrastructure.outbound.persistence.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
// AÑADE ESTE IMPORT
import backend.infrastructure.outbound.persistence.tables.UserAccountsTable

object ReviewsTable : Table("reviews") {
    val id = long("id").autoIncrement()

    // Ahora esto compilará sin problemas
    val studentId = long("student_id").references(UserAccountsTable.id)
    val teacherId = long("teacher_id").references(UserAccountsTable.id)

    // Esto ya está correcto, soluciona el error Int/Short
    val rating = integer("rating")

    val comment = text("comment").nullable()

    val createdAt = timestampWithTimeZone("created_at")

    override val primaryKey = PrimaryKey(id)
}