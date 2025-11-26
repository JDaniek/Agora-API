package backend.infrastructure.inbound.http.dto.reviews

@kotlinx.serialization.Serializable
data class StudentReviewSummaryResponse(
    val studentId: Long,
    val averageRating: Double,
    val totalReviews: Int
)