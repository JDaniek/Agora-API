package backend.infrastructure.outbound.persistence.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentTimestampWithTimeZone
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

/**
 * Tabla student_reviews:
 *
 * - student_id = alumno evaluado
 * - teacher_id = asesor que deja la reseña
 */
object StudentReviewsTable : Table("student_reviews") {
    val id = long("id").autoIncrement()

    val studentId = long("student_id")
        .references(UserAccountsTable.id, onDelete = ReferenceOption.CASCADE)

    val teacherId = long("teacher_id")
        .references(UserAccountsTable.id, onDelete = ReferenceOption.CASCADE)

    val rating = integer("rating")
    val comment = text("comment").nullable()

    val createdAt = timestampWithTimeZone("created_at")
        .defaultExpression(CurrentTimestampWithTimeZone)

    override val primaryKey = PrimaryKey(id)
}