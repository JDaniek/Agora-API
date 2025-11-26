package backend.domain.port.outbound

import backend.domain.model.StudentReview
import backend.domain.model.StudentReviewDetails
import backend.domain.model.StudentReviewSummary

interface StudentReviewRepository {

    suspend fun create(
        studentId: Long,
        teacherId: Long,
        rating: Int,
        comment: String?
    ): StudentReview

    /**
     * Verifica si este asesor ya dejó reseña a este alumno.
     */
    suspend fun hasTeacherReviewForStudent(
        teacherId: Long,
        studentId: Long
    ): Boolean

    /**
     * Lista de reseñas que un alumno ha recibido (para mostrarle su historial).
     */
    suspend fun findForStudent(
        studentId: Long
    ): List<StudentReviewDetails>


    suspend fun getSummaryForStudent(
        studentId: Long
    ): StudentReviewSummary?
}