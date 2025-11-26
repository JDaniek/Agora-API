package backend.infrastructure.inbound.http.mapper.reviews

import backend.domain.model.ReviewDetails
import backend.infrastructure.inbound.http.dto.reviews.ReviewResponse
import java.time.format.DateTimeFormatter
import backend.domain.model.TeacherReviewSummary
import backend.infrastructure.inbound.http.dto.reviews.TeacherReviewSummaryResponse
import backend.domain.model.StudentReviewSummary
import backend.infrastructure.inbound.http.dto.reviews.StudentReviewSummaryResponse
private val instantFormatter: DateTimeFormatter = DateTimeFormatter.ISO_INSTANT

fun ReviewDetails.toResponse(): ReviewResponse =
    ReviewResponse(
        id = id,
        studentId = studentId,
        studentFullName = studentFullName,
        rating = rating,
        comment = comment,
        createdAt = instantFormatter.format(createdAt)
    )


fun TeacherReviewSummary.toResponse(): TeacherReviewSummaryResponse =
    TeacherReviewSummaryResponse(
        teacherId = teacherId,
        averageRating = averageRating,
        totalReviews = totalReviews
    )

fun StudentReviewSummary.toStudentSummaryResponse(): StudentReviewSummaryResponse =
    StudentReviewSummaryResponse(
        studentId = studentId,
        averageRating = averageRating,
        totalReviews = totalReviews
    )

