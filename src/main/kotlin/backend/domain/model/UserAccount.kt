package org.agora.backend.domain.models

import java.time.Instant

data class UserAccount (
    val id: Long,
    val firstName: String,
    val secondName: String?,
    val lastName: String,
    val email: String,
    val passwordHash: String,
    val roleId: Int,
    val isActive: Boolean,
    val createdAt: Instant
)
