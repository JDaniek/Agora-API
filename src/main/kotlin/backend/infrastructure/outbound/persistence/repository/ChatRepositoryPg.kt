package backend.infrastructure.outbound.persistence.repository

import backend.domain.port.outbound.ChatRepository
import backend.infrastructure.outbound.persistence.tables.ChatMembersTable
import backend.infrastructure.outbound.persistence.tables.ChatsTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

// 1. IMPORTA TUS TABLAS REALES (con sus paquetes correctos)
import backend.infrastructure.outbound.persistence.tables.ChatMessagesTable
import backend.infrastructure.outbound.persistence.tables.UserAccountsTable

// 2. IMPORTA TUS MODELOS DE DOMINIO (con sus paquetes correctos)
import backend.domain.model.ChatMessage
import org.jetbrains.exposed.sql.selectAll

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant // Lo usamos para 'now'
import java.time.OffsetDateTime // Lo usamos para 'now'
import java.time.ZoneOffset // Para convertir

/**
 * Implementación de 'ChatRepository' usando PostgreSQL y Exposed.
 */
class ChatRepositoryPg : ChatRepository {

    // (Tu helper dbQuery)
    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction { block() }

    override suspend fun createPrivateChat(userOneId: Long, userTwoId: Long): Result<Long> {
        // ... tu implementación existente ...
        return Result.success(1L)
    }

    override suspend fun createMessage(chatId: Long, senderId: Long, body: String): Result<ChatMessage> = dbQuery {
        try {
            // 3. Usamos Instant.now() que es el tipo de dato de tu modelo
            val now = Instant.now()

            val generatedId = ChatMessagesTable.insert {
                it[ChatMessagesTable.chatId] = chatId
                it[ChatMessagesTable.senderId] = senderId
                it[ChatMessagesTable.body] = body

                // 4. Convertimos Instant a OffsetDateTime para la DB
                it[sentAt] = now.atOffset(ZoneOffset.UTC)
            } get ChatMessagesTable.messageId // 5. Usamos 'messageId' (el nombre de tu columna)

            val chatMessage = ChatMessage(
                messageId = generatedId,
                chatId = chatId,
                senderId = senderId,
                body = body,
                sentAt = now // Devolvemos el Instant
            )
            Result.success(chatMessage)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getChatMessages(chatId: Long): Result<List<ChatMessage>> = dbQuery {
        try {
            val messages = ChatMessagesTable
                .selectAll().where { ChatMessagesTable.chatId eq chatId }
                .orderBy(ChatMessagesTable.sentAt to SortOrder.ASC)
                .map(::rowToChatMessage) // Usamos un mapper

            Result.success(messages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isUserMemberOfChat(userId: Long, chatId: Long): Boolean = dbQuery {
        // 6. Usamos el nombre correcto 'ChatMembersTable' (plural)
        ChatMembersTable.selectAll()
            .where { (ChatMembersTable.userId eq userId) and (ChatMembersTable.chatId eq chatId) }.count() > 0
    }

    // Mapeo de Fila a Modelo de Dominio
    private fun rowToChatMessage(row: ResultRow): ChatMessage {
        return ChatMessage(
            messageId = row[ChatMessagesTable.messageId], // 7. Usamos 'messageId'
            chatId = row[ChatMessagesTable.chatId],
            senderId = row[ChatMessagesTable.senderId],
            body = row[ChatMessagesTable.body],
            // 8. Convertimos OffsetDateTime (de la DB) a Instant (de tu modelo)
            sentAt = row[ChatMessagesTable.sentAt].toInstant()
        )
    }
}