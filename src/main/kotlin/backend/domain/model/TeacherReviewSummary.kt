package backend.domain.model

data class TeacherReviewSummary(
    val teacherId: Long,
    val averageRating: Double,
    val totalReviews: Long
)
