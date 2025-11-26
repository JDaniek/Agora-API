package backend.domain.port.inbound

import backend.domain.model.StudentReviewSummary

interface GetStudentReviewSummaryQuery {
    suspend fun getSummary(studentId: Long): Result<StudentReviewSummary?>
}
