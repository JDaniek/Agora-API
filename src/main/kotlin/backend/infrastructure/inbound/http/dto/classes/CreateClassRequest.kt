package backend.infrastructure.inbound.http.dto.classes

import kotlinx.serialization.Serializable

@Serializable
data class EnrollStudentRequest(
    val studentId: Long
)
