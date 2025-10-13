package org.agora.backend.domain.models

import java.time.Instant

data class ChatMessage (
    val id: Long,
    val chatId: Long,
    val senderId: Long,
    val content: String,
    val sentAt: Instant

)