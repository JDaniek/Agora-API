package backend.application.usecase.reviews

import backend.domain.model.StudentReviewDetails
import backend.domain.port.inbound.GetStudentReviewsForStudentQuery
import backend.domain.port.outbound.StudentReviewRepository

class GetStudentReviewsForStudentQueryImpl(
    private val studentReviewRepository: StudentReviewRepository
) : GetStudentReviewsForStudentQuery {

    override suspend fun getForStudent(studentId: Long): Result<List<StudentReviewDetails>> =
        runCatching {
            studentReviewRepository.findForStudent(studentId)
        }
}