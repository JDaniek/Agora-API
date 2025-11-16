package backend.infrastructure.outbound.persistence.tables

import org.jetbrains.exposed.sql.Table

object RolesTable : Table("roles") {
    // CORRECCIÓN: Cambiado de integer() a long()
    // para que coincida con la llave foránea en UserAccountsTable.
    val id = long("id").autoIncrement()
    val name = text("name").uniqueIndex() // Usamos esta versión que tiene el uniqueIndex
    override val primaryKey = PrimaryKey(id)
}