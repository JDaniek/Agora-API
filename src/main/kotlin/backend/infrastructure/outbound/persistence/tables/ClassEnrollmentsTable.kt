package backend.infrastructure.outbound.persistence.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentTimestampWithTimeZone
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

/**
 * Mapea la tabla class_enrollments.
 *
 * Asunción:
 *  - user_id    = tutor que “posee” la clase
 *  - student_id = alumno inscrito
 */
object ClassEnrollmentsTable : Table("class_enrollments") {

    val userId = long("user_id")
        .references(UserAccountsTable.id, onDelete = ReferenceOption.CASCADE)

    val classId = long("class_id")
        .references(ClassesTable.id, onDelete = ReferenceOption.CASCADE)

    val studentId = long("student_id")
        .references(UserAccountsTable.id, onDelete = ReferenceOption.CASCADE)

    val status = text("status")
        .default("confirmed")  // 🔥 Alineado con la BD

    val createdAt = timestampWithTimeZone("created_at")
        .defaultExpression(CurrentTimestampWithTimeZone)

    // 🔥 PRIMARY KEY alineada con la BD real (class_id, student_id)
    override val primaryKey = PrimaryKey(classId, studentId)
}

