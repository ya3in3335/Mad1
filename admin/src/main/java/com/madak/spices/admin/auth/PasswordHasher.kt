package com.madak.spices.admin.auth

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Salted PBKDF2 password hashing. Only the hash, salt, algorithm and iteration count are stored —
 * never the passphrase itself. There are no built-in/default admin credentials.
 */
object PasswordHasher {
    const val DEFAULT_ITERATIONS = 120_000
    private const val KEY_LENGTH_BITS = 256
    private const val PREFERRED = "PBKDF2WithHmacSHA256"
    private const val FALLBACK = "PBKDF2WithHmacSHA1" // API 24–25 do not ship the SHA-256 variant.

    data class Hash(val algorithm: String, val iterations: Int, val saltHex: String, val hashHex: String)

    fun newSalt(): ByteArray = ByteArray(16).also { SecureRandom().nextBytes(it) }

    fun hash(password: CharArray, salt: ByteArray = newSalt(), iterations: Int = DEFAULT_ITERATIONS, algorithm: String? = null): Hash {
        val algo = algorithm ?: bestAlgorithm()
        val spec = PBEKeySpec(password, salt, iterations, KEY_LENGTH_BITS)
        try {
            val bytes = SecretKeyFactory.getInstance(algo).generateSecret(spec).encoded
            return Hash(algo, iterations, salt.toHex(), bytes.toHex())
        } finally {
            spec.clearPassword()
        }
    }

    fun verify(password: CharArray, stored: Hash): Boolean {
        val candidate = hash(password, stored.saltHex.hexToBytes(), stored.iterations, stored.algorithm)
        return MessageDigest.isEqual(candidate.hashHex.hexToBytes(), stored.hashHex.hexToBytes())
    }

    /** Minimum policy: 8+ characters with at least one letter and one digit. */
    fun isStrongEnough(password: String): Boolean =
        password.length >= 8 && password.any { it.isLetter() } && password.any { it.isDigit() }

    private fun bestAlgorithm(): String = try {
        SecretKeyFactory.getInstance(PREFERRED); PREFERRED
    } catch (_: NoSuchAlgorithmException) {
        FALLBACK
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
    private fun String.hexToBytes(): ByteArray = chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}
