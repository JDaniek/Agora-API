package backend.infrastructure.outbound.persistence.repository

import backend.domain.model.AdviserCard
import backend.domain.port.outbound.TeacherFavoritesRepository
import backend.infrastructure.outbound.persistence.tables.ProfilesTable
import backend.infrastructure.outbound.persistence.tables.SpecialtiesTable
import backend.infrastructure.outbound.persistence.tables.TeacherFavoritesTable
import backend.infrastructure.outbound.persistence.tables.UserAccountsTable
import backend.infrastructure.outbound.persistence.tables.UserSpecialtiesTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.AndOp
import org.jetbrains.exposed.sql.GroupConcat
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.alias
import org.jetbrains.exposed.sql.get
import org.jetbrains.exposed.sql.ResultRow

class TeacherFavoritesRepositoryPg : TeacherFavoritesRepository {

    private suspend fun <T> tx(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    // Debe devolver Unit explícitamente (cuerpo con llaves)
    override suspend fun addFavorite(studentId: Long, teacherId: Long) {
        tx {
            TeacherFavoritesTable.insertIgnore { row ->
                row[TeacherFavoritesTable.studentId] = studentId
                row[TeacherFavoritesTable.teacherId] = teacherId
            }
        }
    }

    override suspend fun removeFavorite(studentId: Long, teacherId: Long) {
        tx {
            TeacherFavoritesTable.deleteWhere {
                (TeacherFavoritesTable.studentId eq studentId) and
                        (TeacherFavoritesTable.teacherId eq teacherId)
            }
        }
    }

    override suspend fun isFavorite(studentId: Long, teacherId: Long): Boolean = tx {
        TeacherFavoritesTable
            .selectAll()
            .where {
                (TeacherFavoritesTable.studentId eq studentId) and
                        (TeacherFavoritesTable.teacherId eq teacherId)
            }
            .limit(1)
            .any()
    }

    override suspend fun findFavoritesForStudent(studentId: Long): List<Long> = tx {
        val teacherIdCol = TeacherFavoritesTable.teacherId

        TeacherFavoritesTable
            .select(teacherIdCol)
            .where { TeacherFavoritesTable.studentId eq studentId }
            .map { row -> row[teacherIdCol] }
    }

    override suspend fun findFavoriteAdvisersForStudent(studentId: Long): List<AdviserCard> = tx {
        // Igual que en searchAdvisers: agregador de especialidades
        val specialtiesAgg = GroupConcat(SpecialtiesTable.name, ", ", false).alias("specialties")

        TeacherFavoritesTable
            .join(
                UserAccountsTable,
                JoinType.INNER,
                onColumn = TeacherFavoritesTable.teacherId,
                otherColumn = UserAccountsTable.id
            )
            .join(
                ProfilesTable,
                JoinType.INNER,
                onColumn = UserAccountsTable.id,
                otherColumn = ProfilesTable.userId
            )
            .join(
                UserSpecialtiesTable,
                JoinType.LEFT,
                onColumn = UserAccountsTable.id,
                otherColumn = UserSpecialtiesTable.userId
            )
            .join(
                SpecialtiesTable,
                JoinType.LEFT,
                onColumn = UserSpecialtiesTable.specialtyId,
                otherColumn = SpecialtiesTable.id
            )
            .select(
                UserAccountsTable.id,
                UserAccountsTable.firstName,
                UserAccountsTable.lastName,
                ProfilesTable.photoUrl,
                ProfilesTable.level,
                ProfilesTable.description,
                specialtiesAgg
            )
            .where {
                TeacherFavoritesTable.studentId eq studentId
            }
            .groupBy(
                UserAccountsTable.id,
                UserAccountsTable.firstName,
                UserAccountsTable.lastName,
                ProfilesTable.photoUrl,
                ProfilesTable.level,
                ProfilesTable.description
            )
            .map { row: ResultRow ->
                val specs = row[specialtiesAgg]  // puede ser String?; lo tratamos abajo
                    ?.split(", ")
                    ?.filter { it.isNotBlank() }
                    ?: emptyList()

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
}
