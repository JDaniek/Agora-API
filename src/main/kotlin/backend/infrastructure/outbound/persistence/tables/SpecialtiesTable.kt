package org.agora.backend.infrastructure.outbound.persistance.tables

import org.jetbrains.exposed.sql.Table
import backend.infrastructure.outbound.persistence.tables.UserAccountsTable

object SpecialtiesTable : Table("specialty") {
    val id = integer("id").autoIncrement()
    val name = text("name")
    override val primaryKey = PrimaryKey(id)
}