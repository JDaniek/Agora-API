package backend.infrastructure.inbound.http.mapper.reviews

import backend.domain.model.StudentReviewDetails
import backend.infrastructure.inbound.http.dto.reviews.StudentReviewResponse
import java.time.format.DateTimeFormatter

private val instantFormatter: DateTimeFormatter =
    DateTimeFormatter.ISO_INSTANT

fun StudentReviewDetails.toStudentResponse(): StudentReviewResponse =
    StudentReviewResponse(
        id = id,
        studentId = studentId,
        teacherId = teacherId,
        teacherFullName = teacherFullName,
        rating = rating,
        comment = comment,
        createdAt = instantFormatter.format(createdAt)
    )