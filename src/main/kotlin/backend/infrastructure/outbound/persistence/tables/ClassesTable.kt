package backend.infrastructure.outbound.persistence.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date

object ClassesTable : Table("classes") {
    val id = long("id").autoIncrement()
    val tutorId = long("tutor_id").references(UserAccountsTable.id, onDelete = ReferenceOption.CASCADE)
    val title = varchar("title", length = 50)
    val description = text("description").nullable()
    val classDate = date("class_date")              // DATE en la BD
    val capacityPerSlot = short("capacity_per_slot")
    val specialtyId = integer("specialty_id").references(SpecialtiesTable.id)
    val isActive = bool("is_active").default(true)
    // Omitimos created_at: la BD lo llena con default now()
    override val primaryKey = PrimaryKey(id)
}
