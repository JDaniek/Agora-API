package backend.application.usecase.reviews

import backend.domain.model.TeacherReviewSummary
import backend.domain.port.inbound.GetTeacherReviewSummaryQuery
import backend.domain.port.outbound.ReviewRepository

class GetTeacherReviewSummaryQueryImpl(
    private val reviewRepository: ReviewRepository
) : GetTeacherReviewSummaryQuery {

    override suspend fun getSummary(teacherId: Long): Result<TeacherReviewSummary> {
        return reviewRepository.getTeacherReviewSummary(teacherId)
            .mapCatching { summary ->
                summary ?: throw NoSuchElementException("Este asesor aún no tiene reseñas")
            }
    }
}
