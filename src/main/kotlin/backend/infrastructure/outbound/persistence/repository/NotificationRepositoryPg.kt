package backend.infrastructure.outbound.persistence.repository

import backend.domain.model.Notification
import backend.domain.model.NotificationDetails
import backend.domain.port.outbound.NotificationRepository
import backend.infrastructure.outbound.persistence.tables.NotificationsTable
import backend.infrastructure.outbound.persistence.tables.NotificationTypesTable
import backend.infrastructure.outbound.persistence.tables.ProfilesTable
import backend.infrastructure.outbound.persistence.tables.UserAccountsTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class NotificationRepositoryPg : NotificationRepository {

    private suspend fun <T> tx(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    private val Sender = UserAccountsTable.alias("sender")
    private val SenderProfile = ProfilesTable.alias("sender_profile")

    override suspend fun createContactRequest(senderId: Long, recipientId: Long): Result<Long> = runCatching {
        tx {
            NotificationsTable.insert {
                it[this.senderId] = senderId
                it[this.recipientId] = recipientId
                it[this.notificationTypeId] = 1 // 1 = 'contact_request'
                it[this.status] = "pending"
            } get NotificationsTable.id
        }
    }

    /**
     * Con filtro opcional por status.
     */
    override suspend fun findNotificationsForUser(
        userId: Long,
        statusFilter: List<String>?
    ): Result<List<NotificationDetails>> = runCatching {
        tx {
            val baseQuery = NotificationsTable
                .join(
                    NotificationTypesTable,
                    JoinType.INNER,
                    NotificationsTable.notificationTypeId,
                    NotificationTypesTable.id
                )
                .join(
                    Sender,
                    JoinType.INNER,
                    NotificationsTable.senderId,
                    Sender[UserAccountsTable.id]
                )
                .join(
                    SenderProfile,
                    JoinType.LEFT,
                    NotificationsTable.senderId,
                    SenderProfile[ProfilesTable.userId]
                )
                .selectAll()
                .where { NotificationsTable.recipientId eq userId }

            val finalQuery = if (!statusFilter.isNullOrEmpty()) {
                baseQuery.andWhere { NotificationsTable.status inList statusFilter }
            } else {
                baseQuery
            }

            finalQuery
                .orderBy(NotificationsTable.createdAt, SortOrder.DESC)
                .map { row ->
                    NotificationDetails(
                        notificationId = row[NotificationsTable.id],
                        status = row[NotificationsTable.status],
                        // 👇 OffsetDateTime -> Instant -> String ISO
                        createdAt = row[NotificationsTable.createdAt].toInstant().toString(),
                        notificationTypeName = row[NotificationTypesTable.name],
                        senderFirstName = row[Sender[UserAccountsTable.firstName]],
                        senderLastName = row[Sender[UserAccountsTable.lastName]],
                        senderPhotoUrl = row[SenderProfile[ProfilesTable.photoUrl]]
                    )
                }
        }
    }

    override suspend fun updateStatus(
        notificationId: Long,
        newStatus: String,
        recipientId: Long
    ): Result<Boolean> = runCatching {
        tx {
            val updatedRows = NotificationsTable.update(
                where = {
                    (NotificationsTable.id eq notificationId) and
                            (NotificationsTable.recipientId eq recipientId)
                }
            ) {
                it[this.status] = newStatus
            }
            updatedRows > 0
        }
    }

    private fun ResultRow.toNotification(): Notification =
        Notification(
            id = this[NotificationsTable.id],
            recipientId = this[NotificationsTable.recipientId],
            senderId = this[NotificationsTable.senderId],
            notificationTypeId = this[NotificationsTable.notificationTypeId],
            status = this[NotificationsTable.status],
            createdAt = this[NotificationsTable.createdAt].toInstant(),
            message = this[NotificationsTable.message],
            chatId = this[NotificationsTable.chatId]
        )

    override suspend fun findById(notificationId: Long): Result<Notification?> = runCatching {
        tx {
            NotificationsTable
                .selectAll()
                .where { NotificationsTable.id eq notificationId }
                .firstOrNull()
                ?.toNotification()
        }
    }

    override suspend fun markAcceptedWithChat(
        notificationId: Long,
        recipientId: Long,
        chatId: Long
    ): Result<Boolean> = runCatching {
        tx {
            val updatedRows = NotificationsTable.update(
                where = {
                    (NotificationsTable.id eq notificationId) and
                            (NotificationsTable.recipientId eq recipientId)
                }
            ) {
                it[this.status] = "accepted"
                it[this.chatId] = chatId
            }
            updatedRows > 0
        }
    }
}
