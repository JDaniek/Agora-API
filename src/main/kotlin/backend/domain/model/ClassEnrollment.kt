package backend.domain.model

import java.time.Instant

data class ClassEnrollment(
    val userId: Long,
    val classId: Long,
    val studentId: Long,
    val status: String,
    val createdAt: Instant
)
