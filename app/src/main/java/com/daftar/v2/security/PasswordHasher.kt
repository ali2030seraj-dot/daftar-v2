package com.daftar.v2.security

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordHasher {

    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH = 256
    private const val SALT_LENGTH = 16

    data class Result(
        val hashBase64: String,
        val saltBase64: String,
        val iterations: Int
    )

    fun hash(
        password: String,
        salt: ByteArray = randomSalt(),
        iterations: Int = ITERATIONS
    ): Result {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(spec).encoded
        return Result(
            hashBase64 = Base64.encodeToString(hash, Base64.NO_WRAP),
            saltBase64 = Base64.encodeToString(salt, Base64.NO_WRAP),
            iterations = iterations
        )
    }

    fun verify(
        password: String,
        hashBase64: String,
        saltBase64: String,
        iterations: Int
    ): Boolean {
        val salt = Base64.decode(saltBase64, Base64.NO_WRAP)
        val expected = Base64.decode(hashBase64, Base64.NO_WRAP)
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, KEY_LENGTH)
        val actual = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            .generateSecret(spec).encoded
        return constantTimeEquals(expected, actual)
    }

    private fun randomSalt(): ByteArray =
        ByteArray(SALT_LENGTH).also { SecureRandom().nextBytes(it) }

    private fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        var diff = 0
        for (i in a.indices) diff = diff or (a[i].toInt() xor b[i].toInt())
        return diff == 0
    }
}