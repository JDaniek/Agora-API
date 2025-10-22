package org.agora.backend.infrastructure.outbound.persistance.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import backend.infrastructure.outbound.persistence.tables.UserAccountsTable
object ProfilesTable : Table("profile") {
    val userId = long("user_id").uniqueIndex().references(UserAccountsTable.id, onDelete = ReferenceOption.CASCADE)
    val description = text("description").nullable()
    val photoUrl = text("photo_url").nullable()
    val stateId = short("state_id").nullable()
    override val primaryKey = PrimaryKey(userId)
}