package backend.domain.port.inbound

import backend.domain.model.TeacherReviewSummary

interface GetTeacherReviewSummaryQuery {
    suspend fun getSummary(teacherId: Long): Result<TeacherReviewSummary>
}
