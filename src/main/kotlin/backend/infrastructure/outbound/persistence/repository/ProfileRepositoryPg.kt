package backend.infrastructure.outbound.persistence.repository

import backend.domain.model.Profile
import backend.domain.port.outbound.ProfileRepository
import backend.infrastructure.outbound.persistence.tables.ProfilesTable
import backend.infrastructure.outbound.persistence.tables.UserSpecialtiesTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class ProfileRepositoryPg : ProfileRepository {

    private suspend fun <T> tx(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    private fun ResultRow.toProfile(): Profile = Profile(
        userId      = this[ProfilesTable.userId],      // Long (BIGINT)
        description = this[ProfilesTable.description],
        photoUrl    = this[ProfilesTable.photoUrl],
        city        = this[ProfilesTable.city],
        stateCode   = this[ProfilesTable.stateCode],
        level       = this[ProfilesTable.level]
    )

    override suspend fun findByUserId(userId: Long): Profile? = tx {
        ProfilesTable
            .selectAll()
            .where { ProfilesTable.userId eq userId }
            .firstOrNull()
            ?.toProfile()
    }

    override suspend fun upsertPhoto(userId: Long, photoUrl: String): Profile = tx {
        val exists = ProfilesTable
            .selectAll()
            .where { ProfilesTable.userId eq userId }
            .firstOrNull()

        if (exists == null) {
            ProfilesTable.insert {
                it[ProfilesTable.userId]   = userId
                it[ProfilesTable.photoUrl] = photoUrl
            }
        } else {
            ProfilesTable.update({ ProfilesTable.userId eq userId }) {
                it[ProfilesTable.photoUrl] = photoUrl
            }
        }

        ProfilesTable
            .selectAll()
            .where { ProfilesTable.userId eq userId }
            .first()
            .toProfile()
    }

    override suspend fun saveOrUpdate(profile: Profile, specialtyIds: List<Long>): Profile = tx {
        val exists = ProfilesTable
            .selectAll()
            .where { ProfilesTable.userId eq profile.userId }
            .firstOrNull()

        if (exists == null) {
            ProfilesTable.insert {
                it[ProfilesTable.userId]     = profile.userId
                it[ProfilesTable.description] = profile.description
                it[ProfilesTable.photoUrl]   = profile.photoUrl
                it[ProfilesTable.city]       = profile.city
                it[ProfilesTable.stateCode]  = profile.stateCode
                it[ProfilesTable.level]      = profile.level
            }
        } else {
            ProfilesTable.update({ ProfilesTable.userId eq profile.userId }) {
                it[ProfilesTable.description] = profile.description
                it[ProfilesTable.photoUrl]    = profile.photoUrl
                it[ProfilesTable.city]        = profile.city
                it[ProfilesTable.stateCode]   = profile.stateCode
                it[ProfilesTable.level]       = profile.level
            }
        }

        // user_specialties.user_id = BIGINT (Long), specialty_id = INTEGER (Int)
        UserSpecialtiesTable.deleteWhere { UserSpecialtiesTable.userId eq profile.userId }

        if (specialtyIds.isNotEmpty()) {
            UserSpecialtiesTable.batchInsert(specialtyIds) { sid ->
                this[UserSpecialtiesTable.userId]      = profile.userId      // Long
                this[UserSpecialtiesTable.specialtyId] = sid.toInt()         // Int
            }
        }

        ProfilesTable
            .selectAll()
            .where { ProfilesTable.userId eq profile.userId }
            .first()
            .toProfile()
    }
}
