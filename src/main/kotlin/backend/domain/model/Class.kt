package backend.domain.model

import java.time.Instant

data class Classes (
    val id: Long,
    val tutorId: Long,
    val specialtyId: Int,
    val tittle: String,
    val description: String,
    val capacity: Int,
    val isActive: Boolean,
    val createdAt: Instant
)