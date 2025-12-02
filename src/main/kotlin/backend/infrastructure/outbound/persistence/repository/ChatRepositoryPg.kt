package backend.infrastructure.outbound.persistence.repository

import backend.domain.model.ChatMessage
import backend.domain.port.outbound.ChatRepository
import backend.infrastructure.inbound.http.dto.chat.MyChatResponse
import backend.infrastructure.outbound.persistence.tables.ChatMembersTable
import backend.infrastructure.outbound.persistence.tables.ChatMessagesTable
import backend.infrastructure.outbound.persistence.tables.ChatsTable
import backend.infrastructure.outbound.persistence.tables.ProfilesTable
import backend.infrastructure.outbound.persistence.tables.UserAccountsTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import backend.infrastructure.outbound.persistence.tables.*
import java.time.Instant
import java.time.ZoneOffset

/**
 * Implementación de 'ChatRepository' usando PostgreSQL y Exposed.
 */
class ChatRepositoryPg : ChatRepository {

    // Helper para transacciones suspendidas
    private suspend fun <T> dbQuery(block: suspend () -> T): T = newSuspendedTransaction { block() }

    /**
     * CREAR CHAT:
     * Inserta la sala y une a los dos usuarios.
     */
    override suspend fun createPrivateChat(userOneId: Long, userTwoId: Long): Result<Long> = dbQuery {
        try {
            // 1. Crear la "Sala" en la tabla 'chats'
            val newChatId = ChatsTable.insert {
                it[chatName] = "Chat Privado"
                // 'createdAt' se llena automático por el default de la tabla
            } get ChatsTable.id

            // 2. Agregar al Usuario 1
            ChatMembersTable.insert {
                it[chatId] = newChatId
                it[userId] = userOneId
            }

            // 3. Agregar al Usuario 2
            ChatMembersTable.insert {
                it[chatId] = newChatId
                it[userId] = userTwoId
            }

            // 4. Devolver el ID del nuevo chat
            Result.success(newChatId)

        } catch (e: Exception) {
            // Aquí podrías manejar si ya existe un chat, etc.
            Result.failure(e)
        }
    }

    /**
     * CREAR MENSAJE:
     * Guarda el mensaje y devuelve el objeto completo con el ID generado.
     */
    override suspend fun createMessage(chatId: Long, senderId: Long, body: String): Result<ChatMessage> = dbQuery {
        try {
            val now = Instant.now()

            val generatedId = ChatMessagesTable.insert {
                it[ChatMessagesTable.chatId] = chatId
                it[ChatMessagesTable.senderId] = senderId
                it[ChatMessagesTable.body] = body
                // Convertimos Instant a OffsetDateTime para la DB (timestamptz)
                it[sentAt] = now.atOffset(ZoneOffset.UTC)
            } get ChatMessagesTable.messageId

            val chatMessage = ChatMessage(
                messageId = generatedId,
                chatId = chatId,
                senderId = senderId,
                body = body,
                sentAt = now // Devolvemos el Instant puro al dominio
            )
            Result.success(chatMessage)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * OBTENER HISTORIAL:
     * Carga los mensajes de un chat.
     */
    override suspend fun getChatMessages(chatId: Long): Result<List<ChatMessage>> = dbQuery {
        try {
            val messages = ChatMessagesTable.selectAll().where { ChatMessagesTable.chatId eq chatId }
                .orderBy(ChatMessagesTable.sentAt to SortOrder.ASC).map(::rowToChatMessage)

            Result.success(messages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * VALIDAR MEMBRESÍA:
     * Seguridad para el WebSocket.
     * INCLUYE LOGS DE DEPURACIÓN.
     */
    override suspend fun isUserMemberOfChat(userId: Long, chatId: Long): Boolean = dbQuery {
        //TRAMPA 3: DENTRO DE LA DB
        println(" DEBUG REPO: Buscando en tabla ChatMembers... User: $userId, Chat: $chatId")

        val count = ChatMembersTable.selectAll()
            .where { (ChatMembersTable.userId eq userId) and (ChatMembersTable.chatId eq chatId) }.count()

        println("DEBUG REPO: Se encontraron $count filas coincidentes.")

        count > 0
    }

    // Helper: Mapeo de Fila a Modelo de Dominio
    private fun rowToChatMessage(row: ResultRow): ChatMessage {
        return ChatMessage(
            messageId = row[ChatMessagesTable.messageId],
            chatId = row[ChatMessagesTable.chatId],
            senderId = row[ChatMessagesTable.senderId],
            body = row[ChatMessagesTable.body],
            // Convertimos OffsetDateTime (de la DB) a Instant
            sentAt = row[ChatMessagesTable.sentAt].toInstant()
        )
    }

    // Nuevo metodo para evitar duplicidad de chats
    override suspend fun findPrivateChatBetweenUsers(
        userOneId: Long,
        userTwoId: Long
    ): Result<Long?> = dbQuery {
        try {
            // 1. Todos los chats donde está el usuario 1
            val userOneChats = ChatMembersTable
                .selectAll()
                .where { ChatMembersTable.userId eq userOneId }
                .map { row -> row[ChatMembersTable.chatId] }
                .toSet()

            // 2. Todos los chats donde está el usuario 2
            val userTwoChats = ChatMembersTable
                .selectAll()
                .where { ChatMembersTable.userId eq userTwoId }
                .map { row -> row[ChatMembersTable.chatId] }
                .toSet()

            // 3. Intersección: chats donde están ambos
            val commonChatId = userOneChats.intersect(userTwoChats).firstOrNull()

            Result.success(commonChatId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getChatsForUser(userId: Long): Result<List<MyChatResponse>> = runCatching {
        transaction {
            // 1. Obtener los IDs de chats donde estoy
            val myChatIds = ChatMembersTable
                .selectAll()
                .where { ChatMembersTable.userId eq userId }
                .map { it[ChatMembersTable.chatId] }

            if (myChatIds.isEmpty()) return@transaction emptyList()

            // 2. Buscar al "otro participante" de esos chats
            (ChatMembersTable innerJoin UserAccountsTable leftJoin ProfilesTable)
                .selectAll()
                .where {
                    (ChatMembersTable.chatId inList myChatIds) and
                            (ChatMembersTable.userId neq userId)
                }
                .map { row ->
                    val chatId = row[ChatMembersTable.chatId]
                    val otherId = row[UserAccountsTable.id]

                    val firstName = row[UserAccountsTable.firstName]
                    val lastName = row[UserAccountsTable.lastName]
                    val photo = row[ProfilesTable.photoUrl]

                    // 3. Buscar último mensaje
                    val lastMsgRow = ChatMessagesTable
                        .selectAll()
                        .where { ChatMessagesTable.chatId eq chatId }
                        .orderBy(ChatMessagesTable.sentAt to SortOrder.DESC)
                        .limit(1)
                        .singleOrNull()

                    val lastMsg = lastMsgRow?.get(ChatMessagesTable.body)
                    val lastTime = lastMsgRow?.get(ChatMessagesTable.sentAt)?.toString()

                    MyChatResponse(
                        chatId = chatId,
                        otherParticipantId = otherId,
                        otherParticipantName = "$firstName $lastName".trim(),
                        otherParticipantPhoto = photo,
                        lastMessage = lastMsg,
                        lastMessageTime = lastTime,
                        unreadCount = 0
                    )
                }
        }
    }

}