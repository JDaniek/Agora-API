package backend.infrastructure.inbound.http.dto.reviews

import kotlinx.serialization.Serializable

@Serializable
data class ReviewCreatedResponse(
    val message: String,
    val reviewId: Long
)