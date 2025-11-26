package backend.infrastructure.outbound.persistence.repository

import backend.domain.model.StudentReview
import backend.domain.model.StudentReviewDetails
import backend.domain.model.StudentReviewSummary
import backend.domain.port.outbound.StudentReviewRepository
import backend.infrastructure.outbound.persistence.tables.StudentReviewsTable
import backend.infrastructure.outbound.persistence.tables.UserAccountsTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class StudentReviewRepositoryPg : StudentReviewRepository {

    private suspend fun <T> tx(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    private fun ResultRow.toStudentReview(): StudentReview =
        StudentReview(
            id = this[StudentReviewsTable.id],
            studentId = this[StudentReviewsTable.studentId],
            teacherId = this[StudentReviewsTable.teacherId],
            rating = this[StudentReviewsTable.rating],
            comment = this[StudentReviewsTable.comment],
            createdAt = this[StudentReviewsTable.createdAt].toInstant()
        )

    private fun ResultRow.toStudentReviewDetails(): StudentReviewDetails {
        val firstName = this[UserAccountsTable.firstName]
        val lastName = this[UserAccountsTable.lastName]

        return StudentReviewDetails(
            id = this[StudentReviewsTable.id],
            studentId = this[StudentReviewsTable.studentId],
            teacherId = this[StudentReviewsTable.teacherId],
            teacherFullName = "$firstName $lastName",
            rating = this[StudentReviewsTable.rating],
            comment = this[StudentReviewsTable.comment],
            createdAt = this[StudentReviewsTable.createdAt].toInstant()
        )
    }

    override suspend fun create(
        studentId: Long,
        teacherId: Long,
        rating: Int,
        comment: String?
    ): StudentReview = tx {
        val newId = StudentReviewsTable.insert { row ->
            row[StudentReviewsTable.studentId] = studentId
            row[StudentReviewsTable.teacherId] = teacherId
            row[StudentReviewsTable.rating] = rating
            row[StudentReviewsTable.comment] = comment
        } get StudentReviewsTable.id

        StudentReviewsTable
            .selectAll()
            .where { StudentReviewsTable.id eq newId }
            .single()
            .toStudentReview()
    }

    override suspend fun hasTeacherReviewForStudent(
        teacherId: Long,
        studentId: Long
    ): Boolean = tx {
        StudentReviewsTable
            .selectAll()
            .where {
                (StudentReviewsTable.teacherId eq teacherId) and
                        (StudentReviewsTable.studentId eq studentId)
            }
            .limit(1)
            .any()
    }

    override suspend fun findForStudent(studentId: Long): List<StudentReviewDetails> = tx {
        StudentReviewsTable
            .join(
                UserAccountsTable,
                JoinType.INNER,
                onColumn = StudentReviewsTable.teacherId,
                otherColumn = UserAccountsTable.id
            )
            .selectAll()
            .where { StudentReviewsTable.studentId eq studentId }
            .orderBy(StudentReviewsTable.createdAt to SortOrder.DESC)
            .map { row -> row.toStudentReviewDetails() }
    }

    override suspend fun getSummaryForStudent(
        studentId: Long
    ): StudentReviewSummary? = tx {
        val avgRating = StudentReviewsTable.rating.avg()
        val total = StudentReviewsTable.id.count()

        StudentReviewsTable
            .select(StudentReviewsTable.studentId, avgRating, total)
            .where { StudentReviewsTable.studentId eq studentId }
            .groupBy(StudentReviewsTable.studentId)
            .firstOrNull()
            ?.let { row ->
                StudentReviewSummary(
                    studentId = row[StudentReviewsTable.studentId],
                    averageRating = row[avgRating]?.toDouble() ?: 0.0,
                    // 👇 el count() es Long, lo convertimos a Int
                    totalReviews = row[total].toInt()
                )
            }
    }
}
