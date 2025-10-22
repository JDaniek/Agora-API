package backend.infrastructure.outbound.persistence.repository

import backend.domain.model.UserAccount
import backend.domain.port.outbound.UserRepository
import backend.infrastructure.outbound.persistence.tables.UserAccountsTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll // <-- Importa esto
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class UserRepositoryPg : UserRepository {

    // Helper para mapear de la tabla de BD a nuestro modelo de dominio
    private fun toUserAccount(row: ResultRow): UserAccount = UserAccount(
        id = row[UserAccountsTable.id], // <-- Ahora es Long
        firstName = row[UserAccountsTable.firstName],
        secondName = row[UserAccountsTable.secondName],
        lastName = row[UserAccountsTable.lastName],
        roleId = row[UserAccountsTable.roleId], // <-- Ahora es Long
        email = row[UserAccountsTable.email],
        passwordHash = row[UserAccountsTable.passwordHash],
        isActive = row[UserAccountsTable.isActive],
        createdAt = row[UserAccountsTable.createdAt]
    )

    override suspend fun findByEmail(email: String): UserAccount? = newSuspendedTransaction {
        UserAccountsTable
            .selectAll() // <-- Sintaxis moderna
            .where { UserAccountsTable.email eq email } // <-- Sintaxis moderna
            .map(::toUserAccount)
            .singleOrNull()
    }

    override suspend fun save(user: UserAccount): UserAccount = newSuspendedTransaction {
        val insertedId = UserAccountsTable.insert {
            // El compilador ya no fallará aquí, porque
            // user.roleId (Long) coincide con UserAccountsTable.roleId (long)
            it[firstName] = user.firstName
            it[secondName] = user.secondName
            it[lastName] = user.lastName
            it[roleId] = user.roleId
            it[email] = user.email
            it[passwordHash] = user.passwordHash
            it[isActive] = user.isActive
        } get UserAccountsTable.id // <-- Esto ahora devuelve un Long

        // insertedId ahora es Long, por lo que coincide con el 'id' de UserAccount
        user.copy(id = insertedId)
    }
}