package backend.domain.port.outbound

import backend.domain.model.Review
import backend.domain.model.ReviewDetails
import backend.domain.model.TeacherReviewSummary

interface ReviewRepository {

    suspend fun create(
        studentId: Long,
        teacherId: Long,
        rating: Int,
        comment: String?
    ): Review

    suspend fun hasStudentReviewForTeacher(
        studentId: Long,
        teacherId: Long
    ): Boolean

    suspend fun findByTeacher(
        teacherId: Long
    ): List<ReviewDetails>

    /**
     * Devuelve el resumen de reseñas de un asesor:
     * promedio y total de reseñas.
     * Si no tiene reseñas, devuelve null.
     */
    suspend fun getTeacherReviewSummary(
        teacherId: Long
    ): Result<TeacherReviewSummary?>


}
