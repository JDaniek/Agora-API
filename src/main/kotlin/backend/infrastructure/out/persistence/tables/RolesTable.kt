package org.agora.backend.infrastructure.outbound.persistance.tables

import org.jetbrains.exposed.sql.Table

object RolesTable : Table("roles") {
    val id = integer("id").autoIncrement()
    val name = text("name")
    override val primaryKey = PrimaryKey(id)
}