package backend.infrastructure.outbound.persistence.repository

import backend.domain.model.AdviserCard
import backend.domain.model.Profile
import backend.domain.port.inbound.SearchAdvisersQuery
import backend.domain.port.outbound.ProfileRepository
import backend.infrastructure.outbound.persistence.tables.ProfilesTable
import backend.infrastructure.outbound.persistence.tables.SpecialtiesTable
import backend.infrastructure.outbound.persistence.tables.UserAccountsTable
import backend.infrastructure.outbound.persistence.tables.UserSpecialtiesTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inSubQuery
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.SortOrder

// ID del rol "Asesor"
private const val ADVISER_ROLE_ID = 2L

class ProfileRepositoryPg : ProfileRepository {

    // ===== Helpers de transacción =====
    private suspend fun <T> tx(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    // ===== Mapeo de filas a dominio =====
    private fun ResultRow.toProfile(): Profile = Profile(
        userId = this[ProfilesTable.userId],
        description = this[ProfilesTable.description],
        photoUrl = this[ProfilesTable.photoUrl],
        city = this[ProfilesTable.city],
        stateCode = this[ProfilesTable.stateCode],
        level = this[ProfilesTable.level]
    )

    // ===== Implementaciones existentes (¡Están perfectas!) =====
    override suspend fun findByUserId(userId: Long): Profile? = tx {
        ProfilesTable
            .selectAll()
            .where { ProfilesTable.userId eq userId }
            .firstOrNull()
            ?.toProfile()
    }

    // (Añadiendo la función upsertPhoto que tenías antes)
    override suspend fun upsertPhoto(userId: Long, photoUrl: String): Unit = tx {
        ProfilesTable.upsert(ProfilesTable.userId) {
            it[ProfilesTable.userId] = userId
            it[ProfilesTable.photoUrl] = photoUrl
        }
    }

    override suspend fun saveOrUpdate(profile: Profile, specialtyIds: List<Long>): Profile = tx {
        ProfilesTable.upsert(ProfilesTable.userId) {
            it[ProfilesTable.userId] = profile.userId
            it[ProfilesTable.description] = profile.description
            it[ProfilesTable.photoUrl] = profile.photoUrl
            it[ProfilesTable.city] = profile.city
            it[ProfilesTable.stateCode] = profile.stateCode
            it[ProfilesTable.level] = profile.level
        }

        UserSpecialtiesTable.deleteWhere { UserSpecialtiesTable.userId eq profile.userId }

        if (specialtyIds.isNotEmpty()) {
            UserSpecialtiesTable.batchInsert(specialtyIds) { sid ->
                this[UserSpecialtiesTable.userId] = profile.userId
                this[UserSpecialtiesTable.specialtyId] = sid.toInt()
            }
        }
        return@tx profile
    }

    // ===== Implementación: searchAdvisers (CORREGIDA CON API MODERNA) =====
    override suspend fun searchAdvisers(
        command: SearchAdvisersQuery.SearchCommand
    ): List<AdviserCard> = tx {

        // CORRECCIÓN 1: GroupConcat (integrado) necesita 'distinct = false'
        val specialtiesAgg = GroupConcat(SpecialtiesTable.name, ", ", false).alias("specialties")

        // CORRECCIÓN 2: Definimos las tablas y las unimos con la sintaxis lambda
        // (que es más robusta que la sintaxis 'on = { ... }')
        val query = UserAccountsTable
            .innerJoin(ProfilesTable) { UserAccountsTable.id eq ProfilesTable.userId }
            .leftJoin(UserSpecialtiesTable) { UserAccountsTable.id eq UserSpecialtiesTable.userId }
            .leftJoin(SpecialtiesTable) { UserSpecialtiesTable.specialtyId eq SpecialtiesTable.id }
            .select( // CORRECCIÓN 3: Usamos .select() (moderno)
                UserAccountsTable.id,
                UserAccountsTable.firstName,
                UserAccountsTable.lastName,
                ProfilesTable.photoUrl,
                ProfilesTable.level,
                ProfilesTable.description,
                specialtiesAgg
            )
            .where { UserAccountsTable.roleId eq ADVISER_ROLE_ID } // Filtro base
            // CORRECCIÓN 4: Debemos agrupar por TODAS las columnas no agregadas
            .groupBy(
                UserAccountsTable.id,
                UserAccountsTable.firstName,
                UserAccountsTable.lastName,
                ProfilesTable.photoUrl,
                ProfilesTable.level,
                ProfilesTable.description
            )

        // --- Aplicar Filtros Dinámicos (como 'apply') ---
        query.apply {
            // Filtro por Texto (q=...)
            command.query?.trim()?.takeIf { it.isNotEmpty() }?.let { q ->
                andWhere {
                    (UserAccountsTable.firstName like "%$q%") or
                            (UserAccountsTable.lastName like "%$q%")
                }
            }

            // Filtro por Estado (state=...)
            command.state?.let { state ->
                andWhere { ProfilesTable.stateCode eq state }
            }

            // Filtro por Nivel (level=...)
            command.level?.let { lvl ->
                andWhere { ProfilesTable.level eq lvl }
            }

            // Filtro por Materias (specialty=...)
            command.specialtyIds?.takeIf { it.isNotEmpty() }?.let { ids ->
                val asInt = ids.map { it.toInt() }
                val subQuery = UserSpecialtiesTable
                    .select(UserSpecialtiesTable.userId)
                    .where { UserSpecialtiesTable.specialtyId inList asInt }

                andWhere { UserAccountsTable.id inSubQuery subQuery }
            }

            // --- Orden y Límite ---
            val noFilters = command.query.isNullOrBlank() &&
                    command.state == null &&
                    command.level == null &&
                    command.specialtyIds.isNullOrEmpty()

            if (noFilters) {
                // CORRECCIÓN 5: Usamos Random() (integrado)
                orderBy(Random() to SortOrder.ASC).limit(32)
            } else {
                limit(100)
            }
        }

        // --- Mapear Resultados ---
        query.map { row ->
            val specs = row.getOrNull(specialtiesAgg)?.split(", ")?.filter { it.isNotBlank() } ?: emptyList()
            AdviserCard(
                userId = row[UserAccountsTable.id],
                firstName = row[UserAccountsTable.firstName],
                lastName = row[UserAccountsTable.lastName],
                photoUrl = row[ProfilesTable.photoUrl],
                level = row[ProfilesTable.level],
                description = row[ProfilesTable.description],
                specialties = specs
            )
        }
    }

    // CORRECCIÓN 6: Eliminamos todas las clases 'CustomFunction' rotas.
    // Ya no son necesarias.
}