package backend.domain.model

data class Notifications (
    val id: Long,
    val userId: Long,
    val message: String,
    val chatId: Long
)