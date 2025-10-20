package org.agora.backend.domain.models

data class Notifications (
    val id: Long,
    val userId: Long,
    val message: String,
    val chatId: Long
)