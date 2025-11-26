package backend.infrastructure.inbound.http.dto.reviews

import kotlinx.serialization.Serializable

@Serializable
data class CreateReviewRequest(
    val rating: Int,
    val comment: String? = null
)

@Serializable
data class ReviewResponse(
    val id: Long,
    val studentId: Long,
    val studentFullName: String,
    val rating: Int,
    val comment: String?,
    val createdAt: String
)