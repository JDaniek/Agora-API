package backend.domain.port.outbound

interface PasswordService {
    fun hashPassword(password: String): String
    fun verifyPassword(password: String, hash: String): Boolean
}