package backend.infrastructure.inbound.http.dto.reviews

import kotlinx.serialization.Serializable

@Serializable
data class TeacherReviewSummaryResponse(
    val teacherId: Long,
    val averageRating: Double,
    val totalReviews: Long
)
