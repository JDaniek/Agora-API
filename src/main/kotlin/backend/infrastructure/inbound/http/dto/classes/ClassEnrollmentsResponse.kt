package backend.infrastructure.inbound.http.dto.classes

import kotlinx.serialization.Serializable

@Serializable
data class ClassEnrollmentResponse(
    val studentId: Long,
    val fullName: String,
    val email: String,
    val photoUrl: String?,
    val status: String,
    val enrolledAt: String
)

@Serializable
data class StudentClassResponse(
    val classId: Long,
    val title: String,
    val description: String?,
    val classDate: String,
    val status: String,
    val tutorId: Long,
    val specialtyId: Int
)