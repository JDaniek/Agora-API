package backend.domain.model

import backend.domain.model.UserAccount // ⬅️ asegura este import

data class Review(
    val id: Int,
    val reviewer: UserAccount, // si así lo modelaste
    val reviewee: UserAccount,
    val rating: Int,
    val comment: String?
)
