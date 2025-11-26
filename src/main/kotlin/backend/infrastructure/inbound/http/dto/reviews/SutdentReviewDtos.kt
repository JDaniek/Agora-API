package backend.infrastructure.inbound.http.dto.reviews

import kotlinx.serialization.Serializable

@Serializable
data class StudentReviewResponse(
    val id: Long,
    val studentId: Long,
    val teacherId: Long,
    val teacherFullName: String,
    val rating: Int,
    val comment: String?,
    val createdAt: String
)