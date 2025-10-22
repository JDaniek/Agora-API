package backend.domain.port.outbound

import backend.domain.model.UserAccount

interface UserRepository {
    suspend fun findByEmail(email: String): UserAccount?
    suspend fun save(user: UserAccount): UserAccount
}