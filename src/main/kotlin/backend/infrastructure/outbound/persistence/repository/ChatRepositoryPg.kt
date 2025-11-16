package backend.infrastructure.outbound.persistence.repository

import backend.domain.port.outbound.ChatRepository
import backend.infrastructure.outbound.persistence.tables.ChatMembersTable
import backend.infrastructure.outbound.persistence.tables.ChatsTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

/**
 * Implementación de 'ChatRepository' usando PostgreSQL y Exposed.
 */
class ChatRepositoryPg : ChatRepository {

    // Helper de transacción (asumiendo que está en este paquete o lo importas)
    private suspend fun <T> tx(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    /**
     * Crea un chat y añade a los dos miembros.
     * Esto se ejecuta en una sola transacción.
     */
    override suspend fun createPrivateChat(userOneId: Long, userTwoId: Long): Result<Long> = runCatching {
        tx {
            // 1. Crear el chat
            val newChatId = ChatsTable.insert {
                // 'chat_name' es nulo para chats 1-a-1
            } get ChatsTable.id

            // 2. Añadir al primer miembro
            ChatMembersTable.insert {
                it[this.chatId] = newChatId
                it[this.userId] = userOneId
            }

            // 3. Añadir al segundo miembro
            ChatMembersTable.insert {
                it[this.chatId] = newChatId
                it[this.userId] = userTwoId
            }

            // 4. Devolver el ID del nuevo chat
            newChatId
        }
    }
}