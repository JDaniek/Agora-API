package org.agora.backend.domain.models

data class Profile(
    val userId: Long,
    val description: String?,
    val photoUrl: String?,
    val stateId: Int?,
)