package backend.infrastructure.outbound.persistence.repository

import backend.domain.model.NotificationDetails
import backend.domain.port.outbound.NotificationRepository
// (¡Ya no necesitamos 'backend.infrastructure.plugins.dbQuery'!)
import backend.infrastructure.outbound.persistence.tables.NotificationsTable
import backend.infrastructure.outbound.persistence.tables.NotificationTypesTable
import backend.infrastructure.outbound.persistence.tables.ProfilesTable
import backend.infrastructure.outbound.persistence.tables.UserAccountsTable
import org.jetbrains.exposed.sql.*
// --- IMPORTS AÑADIDOS PARA EL HELPER 'tx' ---
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

/**
 * Implementación de 'NotificationRepository' usando PostgreSQL y Exposed.
 */
class NotificationRepositoryPg : NotificationRepository {

    // --- HELPER DE TRANSACCIÓN (copiado de tu ProfileRepositoryPg) ---
    private suspend fun <T> tx(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    // --- Aliases para los JOINs ---
    private val Sender = UserAccountsTable.alias("sender")
    private val SenderProfile = ProfilesTable.alias("sender_profile")

    /**
     * Crea una solicitud de contacto (INSERT)
     */
    override suspend fun createContactRequest(senderId: Long, recipientId: Long): Result<Long> = runCatching {
        // Usamos 'tx' en lugar de 'dbQuery'
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
     * Obtiene la lista de notificaciones para la campana (SELECT + JOINs)
     */
    override suspend fun findNotificationsForUser(userId: Long): Result<List<NotificationDetails>> = runCatching {
        tx {
            NotificationsTable
                .join(NotificationTypesTable, JoinType.INNER, NotificationsTable.notificationTypeId, NotificationTypesTable.id)
                .join(Sender, JoinType.INNER, NotificationsTable.senderId, Sender[UserAccountsTable.id])
                .join(SenderProfile, JoinType.LEFT, NotificationsTable.senderId, SenderProfile[ProfilesTable.userId])
                .selectAll()
                .where { NotificationsTable.recipientId eq userId }
                .orderBy(NotificationsTable.createdAt, SortOrder.DESC)
                .map { row ->
                    NotificationDetails(
                        notificationId = row[NotificationsTable.id],
                        status = row[NotificationsTable.status],
                        createdAt = row[NotificationsTable.createdAt].toInstant(),
                        notificationTypeName = row[NotificationTypesTable.name],
                        senderFirstName = row[Sender[UserAccountsTable.firstName]],
                        senderLastName = row[Sender[UserAccountsTable.lastName]],
                        senderPhotoUrl = row[SenderProfile[ProfilesTable.photoUrl]]
                    )
                }
        }
    }

    /**
     * Actualiza el estado de una notificación (UPDATE)
     */
    override suspend fun updateStatus(notificationId: Long, newStatus: String, recipientId: Long): Result<Boolean> = runCatching {
        tx {
            val updatedRows = NotificationsTable.update(
                where = {
                    (NotificationsTable.id eq notificationId) and (NotificationsTable.recipientId eq recipientId)
                }
            ) {
                it[this.status] = newStatus
            }
            updatedRows > 0
        }
    }
}