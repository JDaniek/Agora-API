package backend.infrastructure.outbound.persistence.repository

import backend.domain.model.Specialty
import backend.domain.port.outbound.SpecialtyRepository
import backend.infrastructure.outbound.persistence.tables.SpecialtiesTable
import backend.infrastructure.outbound.persistence.tables.UserSpecialtiesTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class SpecialtyRepositoryPg : SpecialtyRepository {

    // Helper para convertir fila a modelo de dominio
    private fun toSpecialty(row: ResultRow): Specialty = Specialty(
        id = row[SpecialtiesTable.id].toLong(),
        name = row[SpecialtiesTable.name]
    )

    /**
     * Implementación de 'findByUserId'.
     * Une UserSpecialties con Specialties para obtener los nombres.
     */
    override suspend fun findByUserId(userId: Long): List<Specialty> = newSuspendedTransaction {
        (UserSpecialtiesTable innerJoin SpecialtiesTable)
            .selectAll()
            .where { UserSpecialtiesTable.userId eq userId }
            .map(::toSpecialty)
    }

    // TODO: Implementar otros métodos (ej. findAll para el formulario)
}
