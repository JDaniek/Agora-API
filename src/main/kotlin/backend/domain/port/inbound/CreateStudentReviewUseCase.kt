package backend.domain.port.inbound

import backend.domain.model.StudentReview

interface CreateStudentReviewUseCase {

    data class Command(
        val teacherId: Long,
        val studentId: Long,
        val rating: Int,
        val comment: String?
    )

    suspend fun execute(command: Command): Result<StudentReview>
}