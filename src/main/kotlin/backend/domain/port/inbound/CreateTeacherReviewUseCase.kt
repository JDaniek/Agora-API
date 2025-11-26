package backend.domain.port.inbound

import backend.domain.model.Review

interface CreateTeacherReviewUseCase {

    data class Command(
        val studentId: Long,
        val teacherId: Long,
        val rating: Int,
        val comment: String?
    )

    suspend fun execute(command: Command): Result<Review>
}