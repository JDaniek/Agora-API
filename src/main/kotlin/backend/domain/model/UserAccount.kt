package backend.domain.model

import java.time.OffsetDateTime

data class UserAccount(
    val id: Long, // <--- DEBE SER Long (para bigint)
    val firstName: String,
    val secondName: String?,
    val lastName: String,
    val roleId: Long, // <--- DEBE SER Long (para bigint)
    val email: String,
    val passwordHash: String,
    val isActive: Boolean,
    val createdAt: OffsetDateTime? // <--- DEBE SER Nullable (?)
)