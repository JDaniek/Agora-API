package backend.infrastructure.outbound.persistence.repository

import backend.domain.model.Review
import backend.domain.model.ReviewDetails
import backend.domain.model.TeacherReviewSummary
import backend.domain.port.outbound.ReviewRepository
import backend.infrastructure.outbound.persistence.tables.ReviewsTable
import backend.infrastructure.outbound.persistence.tables.UserAccountsTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant

class ReviewRepositoryPg : ReviewRepository {

    private suspend fun <T> tx(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    private fun ResultRow.toReview(): Review =
        Review(
            id = this[ReviewsTable.id],
            studentId = this[ReviewsTable.studentId],
            teacherId = this[ReviewsTable.teacherId],
            rating = this[ReviewsTable.rating],
            comment = this[ReviewsTable.comment],
            createdAt = this[ReviewsTable.createdAt].toInstant()
        )

    private fun ResultRow.toReviewDetails(): ReviewDetails =
        ReviewDetails(
            id = this[ReviewsTable.id],
            studentId = this[ReviewsTable.studentId],
            studentFullName = this[UserAccountsTable.firstName] + " " + this[UserAccountsTable.lastName],
            rating = this[ReviewsTable.rating],
            comment = this[ReviewsTable.comment],
            createdAt = this[ReviewsTable.createdAt].toInstant()
        )

    override suspend fun create(
        studentId: Long,
        teacherId: Long,
        rating: Int,
        comment: String?
    ): Review = tx {
        val newId = ReviewsTable.insert {
            it[this.studentId] = studentId
            it[this.teacherId] = teacherId
            it[this.rating] = rating
            it[this.comment] = comment
        } get ReviewsTable.id

        ReviewsTable
            .selectAll()
            .where { ReviewsTable.id eq newId }
            .single()
            .toReview()
    }

    override suspend fun hasStudentReviewForTeacher(
        studentId: Long,
        teacherId: Long
    ): Boolean = tx {
        ReviewsTable
            .selectAll()
            .where {
                (ReviewsTable.studentId eq studentId) and
                        (ReviewsTable.teacherId eq teacherId)
            }
            .limit(1)
            .any()
    }

    override suspend fun findByTeacher(
        teacherId: Long
    ): List<ReviewDetails> = tx {
        ReviewsTable
            .join(
                UserAccountsTable,
                JoinType.INNER,
                onColumn = ReviewsTable.studentId,
                otherColumn = UserAccountsTable.id
            )
            .selectAll()
            .where { ReviewsTable.teacherId eq teacherId }
            .orderBy(ReviewsTable.createdAt to SortOrder.DESC)
            .map { row -> row.toReviewDetails() }
    }

    override suspend fun getTeacherReviewSummary(
        teacherId: Long
    ): Result<TeacherReviewSummary?> = runCatching {
        tx {
            // Expresiones de agregación
            val avgExpr = ReviewsTable.rating.avg()
            val countExpr = ReviewsTable.id.count()

            ReviewsTable
                .select(
                    ReviewsTable.teacherId,
                    avgExpr,
                    countExpr
                )
                .where { ReviewsTable.teacherId eq teacherId }
                .groupBy(ReviewsTable.teacherId)
                .firstOrNull()
                ?.let { row ->
                    TeacherReviewSummary(
                        teacherId = row[ReviewsTable.teacherId],
                        averageRating = row[avgExpr]?.toDouble() ?: 0.0,
                        totalReviews = row[countExpr].toLong()
                    )
                }
        }
    }


}