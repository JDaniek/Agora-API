package backend.infrastructure.security

import at.favre.lib.crypto.bcrypt.BCrypt
import backend.domain.port.outbound.PasswordService

class PasswordServiceImpl : PasswordService {
    private val bCrypt = BCrypt.withDefaults()
    private val cost = 12 // Costo de hashing

    override fun hashPassword(password: String): String {
        return bCrypt.hashToString(cost, password.toCharArray())
    }

    override fun verifyPassword(password: String, hash: String): Boolean {
        return BCrypt.verifyer().verify(password.toCharArray(), hash).verified
    }
}