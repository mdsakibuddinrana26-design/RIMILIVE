package com.example.core.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

object PasswordSecurity {

    private val secureRandom = SecureRandom()

    fun generateSalt(): String {
        val saltBytes = ByteArray(16)
        secureRandom.nextBytes(saltBytes)
        return Base64.getEncoder().encodeToString(saltBytes)
    }

    fun hashPassword(password: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(salt.toByteArray(Charsets.UTF_8))
        val digest = md.digest(password.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun verifyPassword(password: String, salt: String, expectedHash: String): Boolean {
        val computedHash = hashPassword(password, salt)
        return computedHash == expectedHash
    }

    /**
     * Generates a 6-digit numeric recovery verification code.
     */
    fun generateRecoveryCode(): String {
        val code = 100000 + secureRandom.nextInt(900000)
        return code.toString()
    }
}
