package backend.domain.model

import java.time.Instant

data class ClassEnrollmentDetails(
    val studentId: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val status: String,
    val enrolledAt: Instant
)