package backend.domain.model

import java.time.Instant

data class Review(
    val id: Long,
    val studentId: Long,
    val teacherId: Long,
    val rating: Int,
    val comment: String?,
    val createdAt: Instant
)

/**
 * Read model para listar reseñas de un asesor,
 * incluyendo nombre del alumno.
 */
data class ReviewDetails(
    val id: Long,
    val studentId: Long,
    val studentFullName: String,
    val rating: Int,
    val comment: String?,
    val createdAt: Instant
)