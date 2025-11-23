package backend.domain.model

import java.time.LocalDate

/**
 * Representa una "clase" o sesión programada por un asesor.
 */
data class ClassSession(
    val id: Long,
    val tutorId: Long,
    val specialtyId: Int,
    val title: String,
    val description: String?,
    val classDate: LocalDate,
    val capacityPerSlot: Int,
    val isActive: Boolean
)


data class ClassEnrollmentForStudent(
    val classId: Long,
    val title: String,
    val description: String?,
    val classDate: LocalDate,
    val status: String,      // 'confirmed', 'pending', 'canceled'
    val tutorId: Long,
    val specialtyId: Int
)

