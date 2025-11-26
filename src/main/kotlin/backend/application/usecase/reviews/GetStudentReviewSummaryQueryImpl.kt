package backend.application.usecase.reviews

import backend.domain.model.StudentReviewSummary
import backend.domain.port.inbound.GetStudentReviewSummaryQuery
import backend.domain.port.outbound.StudentReviewRepository

class GetStudentReviewSummaryQueryImpl(
    private val studentReviewRepository: StudentReviewRepository
) : GetStudentReviewSummaryQuery {

    override suspend fun getSummary(studentId: Long): Result<StudentReviewSummary?> =
        runCatching {
            studentReviewRepository.getSummaryForStudent(studentId)
        }
}
