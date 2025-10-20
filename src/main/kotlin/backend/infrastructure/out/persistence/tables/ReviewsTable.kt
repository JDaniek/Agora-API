package org.agora.backend.infrastructure.outbound.persistance.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object ReviewsTable : Table("reviews") {
    val id = integer("id").autoIncrement()
    val rating = short("rating")
    val comment = varchar("comment", 200).nullable()
    val createdAt = timestamp("created_at").default(Instant.now())
    val updatedAt = timestamp("updated_at").nullable()
    val classId = long("class_id").references(ClassesTable.id, onDelete = ReferenceOption.CASCADE)
    val reviewerId = long("reviewer_id").references(UserAccountsTable.id, onDelete = ReferenceOption.CASCADE)
    val revieweeId = long("reviewee_id").references(UserAccountsTable.id, onDelete = ReferenceOption.CASCADE)
    override val primaryKey = PrimaryKey(id)
    init {
        check("rating_check") { rating.between(1, 5) }
    }
}