package backend.domain.model

import java.time.Instant

data class Media (
    val id: Long,
    val uploadedAt: Instant,
    val url: String,
    val createdAt: Instant
)