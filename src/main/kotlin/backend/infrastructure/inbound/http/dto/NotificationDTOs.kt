package backend.infrastructure.inbound.http.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateNotificationStatusRequest(
    val status: String // "accepted" o "declined"
)

@Serializable
data class NewChatResponse(
    val chatId: Long
)