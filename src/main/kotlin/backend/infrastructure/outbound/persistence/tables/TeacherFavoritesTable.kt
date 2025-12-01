package backend.infrastructure.outbound.persistence.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentTimestampWithTimeZone
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object TeacherFavoritesTable : Table("teacher_favorites") {

    val studentId = long("student_id")
        .references(UserAccountsTable.id, onDelete = ReferenceOption.CASCADE)

    val teacherId = long("teacher_id")
        .references(UserAccountsTable.id, onDelete = ReferenceOption.CASCADE)

    val createdAt = timestampWithTimeZone("created_at")
        .defaultExpression(CurrentTimestampWithTimeZone)

    override val primaryKey = PrimaryKey(studentId, teacherId, name = "pk_teacher_favorites")
}
