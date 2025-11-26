package backend.domain.port.inbound

import backend.domain.model.StudentReviewDetails

interface GetStudentReviewsForStudentQuery {
    suspend fun getForStudent(studentId: Long): Result<List<StudentReviewDetails>>
}