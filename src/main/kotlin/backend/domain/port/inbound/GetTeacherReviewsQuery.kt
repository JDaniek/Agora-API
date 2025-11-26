package backend.domain.port.inbound

import backend.domain.model.ReviewDetails

interface GetTeacherReviewsQuery {
    suspend fun getForTeacher(teacherId: Long): Result<List<ReviewDetails>>
}