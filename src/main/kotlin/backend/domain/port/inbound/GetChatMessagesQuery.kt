package backend.domain.port.inbound

import backend.domain.model.ChatMessage

interface GetChatMessagesQuery {
    suspend fun execute(chatId: Long, requesterId: Long): Result<List<ChatMessage>>
}