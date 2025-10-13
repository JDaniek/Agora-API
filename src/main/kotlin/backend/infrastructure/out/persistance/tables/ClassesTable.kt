package org.agora.backend.infrastructure.out.persistance.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object ClassesTable : Table("classes") {
    val id = long("id").autoIncrement()
    val title = varchar("title", 50)
    val description = text("description").nullable()
    val capacityPerSlot = short("capacity_per_slot")
    val isActive = bool("is_active").default(true)
    val tutorId = long("tutor_id").references(UserAccountsTable.id, onDelete = ReferenceOption.CASCADE)
    val specialtyId = integer("specialty_id").references(SpecialtiesTable.id)
    override val primaryKey = PrimaryKey(id)
}