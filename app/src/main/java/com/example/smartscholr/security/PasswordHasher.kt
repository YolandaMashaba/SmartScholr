package com.example.smartscholr.security

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordHasher {
    private const val ITERATIONS = 65_536
    private const val SALT_BYTES = 16
    private const val HASH_BYTES = 32

    fun hash(password: String): Pair<String, String> {
        val salt = ByteArray(SALT_BYTES).also { SecureRandom().nextBytes(it) }
        val hash = derive(password, salt)
        return Base64.encodeToString(salt, Base64.NO_WRAP) to Base64.encodeToString(hash, Base64.NO_WRAP)
    }

    fun verify(password: String, saltB64: String, hashB64: String): Boolean {
        return try {
            val salt = Base64.decode(saltB64, Base64.NO_WRAP)
            val expected = Base64.decode(hashB64, Base64.NO_WRAP)
            derive(password, salt).contentEquals(expected)
        } catch (_: IllegalArgumentException) {
            false
        } catch (_: java.security.GeneralSecurityException) {
            false
        }
    }

    private fun derive(password: String, salt: ByteArray): ByteArray {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, HASH_BYTES * 8)
        return factory.generateSecret(spec).encoded
    }
}
