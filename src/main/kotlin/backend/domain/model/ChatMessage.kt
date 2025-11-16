package backend.domain.model

import java.time.Instant

data class ChatMessage (
    // --- CORREGIDO ---
    val messageId: Long, // Cambiado de 'id'
    // -----------------
    val chatId: Long,
    val senderId: Long,
    // --- CORREGIDO ---
    val body: String,    // Cambiado de 'content'
    // -----------------
    val sentAt: Instant
)