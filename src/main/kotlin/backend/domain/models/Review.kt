package org.agora.backend.domain.models

import java.time.Instant

data class Review (
    val id: Long,
    val studentId: UserAccount,
    val teacherId: UserAccount,
    val rating: Int,
    val review: String,
    val createdAt: Instant,
    val updatedAt: Instant
)