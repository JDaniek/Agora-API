package org.agora.backend.infrastructure.outbound.persistance.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object ClassEnrollmentsTable : Table("class_enrollments") {
    // NOTA: Estructura mejorada como se sugirió.
    val studentId = long("student_id").references(UserAccountsTable.id, onDelete = ReferenceOption.CASCADE)
    val classId = long("class_id").references(ClassesTable.id, onDelete = ReferenceOption.CASCADE)
    override val primaryKey = PrimaryKey(studentId, classId)
}