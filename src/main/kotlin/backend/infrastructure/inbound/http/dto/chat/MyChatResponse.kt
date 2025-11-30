package backend.infrastructure.inbound.http.dto.chat

import kotlinx.serialization.Serializable

@Serializable
data class MyChatResponse(
    val chatId: Long,
    val otherParticipantId: Long,
    val otherParticipantName: String,
    val otherParticipantPhoto: String?,
    val lastMessage: String?,
    val lastMessageTime: String?, // ISO String
    val unreadCount: Int = 0 // Por ahora 0, luego implementamos lógica de leídos
)