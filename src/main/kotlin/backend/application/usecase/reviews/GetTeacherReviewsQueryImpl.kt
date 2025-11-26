package backend.application.usecase.reviews

import backend.domain.model.ReviewDetails
import backend.domain.port.inbound.GetTeacherReviewsQuery
import backend.domain.port.outbound.ReviewRepository

class GetTeacherReviewsQueryImpl(
    private val reviewRepository: ReviewRepository
) : GetTeacherReviewsQuery {

    override suspend fun getForTeacher(teacherId: Long): Result<List<ReviewDetails>> =
        runCatching {
            reviewRepository.findByTeacher(teacherId)
        }
}