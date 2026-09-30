package com.daftar.v2.backup

import android.util.Base64
import kotlinx.serialization.Serializable
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * تشفير النسخ الاحتياطية بـ AES-256-GCM.
 * مفتاح مشتق من كلمة المرور بـ PBKDF2 (100,000 دورة).
 */
object BackupCrypto {

    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val KEY_ALGORITHM = "AES"
    private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val KEY_SIZE = 256
    private const val ITERATIONS = 100_000
    private const val SALT_LENGTH = 16
    private const val IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128

    @Serializable
    data class EncryptedData(
        val format: String,
        val salt: String,
        val iv: String,
        val payload: String
    )

    /**
     * يُشفّر نصًا بكلمة مرور.
     */
    fun encrypt(plaintext: String, password: String): EncryptedData {
        val salt = ByteArray(SALT_LENGTH).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(IV_LENGTH).also { SecureRandom().nextBytes(it) }

        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

        return EncryptedData(
            format = "daftar-backup-encrypted-v1",
            salt = Base64.encodeToString(salt, Base64.NO_WRAP),
            iv = Base64.encodeToString(iv, Base64.NO_WRAP),
            payload = Base64.encodeToString(ciphertext, Base64.NO_WRAP)
        )
    }

    /**
     * يُفكّ التشفير.
     * @throws javax.crypto.AEADBadTagException إذا كانت كلمة المرور خاطئة.
     */
    fun decrypt(data: EncryptedData, password: String): String {
        val salt = Base64.decode(data.salt, Base64.NO_WRAP)
        val iv = Base64.decode(data.iv, Base64.NO_WRAP)
        val ciphertext = Base64.decode(data.payload, Base64.NO_WRAP)

        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        val plaintext = cipher.doFinal(ciphertext)

        return String(plaintext, Charsets.UTF_8)
    }

    private fun deriveKey(password: String, salt: ByteArray): SecretKey {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_SIZE)
        val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, KEY_ALGORITHM)
    }
}