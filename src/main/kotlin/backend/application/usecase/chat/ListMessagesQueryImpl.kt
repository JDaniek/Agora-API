package backend.application.usecase.chat

import backend.domain.model.ChatMessage
import backend.domain.port.inbound.GetChatMessagesQuery
import backend.domain.port.outbound.ChatRepository

class ListMessagesQueryImpl(
    private val chatRepository: ChatRepository
) : GetChatMessagesQuery {

    override suspend fun execute(chatId: Long, requesterId: Long): Result<List<ChatMessage>> {
        val isMember = chatRepository.isUserMemberOfChat(requesterId, chatId)
        if (!isMember) {
            return Result.failure(SecurityException("Usuario no autorizado en este chat"))
        }
        return chatRepository.getChatMessages(chatId)
    }
}