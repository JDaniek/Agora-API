package backend.domain.model

import java.time.Instant

/**
 * Reseña que un ASESOR deja sobre un ALUMNO.
 *
 * - studentId  = alumno evaluado
 * - teacherId  = asesor que evalúa
 */
data class StudentReview(
    val id: Long,
    val studentId: Long,
    val teacherId: Long,
    val rating: Int,
    val comment: String?,
    val createdAt: Instant
)

/**
 * Read model para listar reseñas de un alumno,
 * incluyendo nombre completo del asesor.
 */
data class StudentReviewDetails(
    val id: Long,
    val studentId: Long,
    val teacherId: Long,
    val teacherFullName: String,
    val rating: Int,
    val comment: String?,
    val createdAt: Instant
)


data class StudentReviewSummary(
    val studentId: Long,
    val averageRating: Double,
    val totalReviews: Int
)