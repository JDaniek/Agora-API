package backend.infrastructure.outbound.persistence.tables
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object UserSpecialtiesTable : Table("user_specialties") {
    val userId = long("user_id").references(UserAccountsTable.id, onDelete = ReferenceOption.RESTRICT)
    val specialtyId = integer("specialty_id").references(SpecialtiesTable.id, onDelete = ReferenceOption.RESTRICT)
    override val primaryKey = PrimaryKey(userId, specialtyId)
}