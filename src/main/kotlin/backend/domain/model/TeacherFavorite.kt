package backend.domain.model

import java.time.Instant

data class TeacherFavorite(
    val studentId: Long,
    val teacherId: Long,
    val createdAt: Instant
)