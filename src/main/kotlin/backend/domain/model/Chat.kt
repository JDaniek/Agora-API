package org.agora.backend.domain.models

import java.time.Instant

data class Chat (
    val id: Long,
    val chatName: String?,
    val createdAt: Instant
)