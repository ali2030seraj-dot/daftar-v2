package com.daftar.v2.security.license

import android.content.Context
import android.provider.Settings
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.MessageDigest
import java.security.PublicKey
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceIdProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val KEYSTORE_ALIAS = "daftar_device_key"
        private const val PREFS_NAME = "daftar_device_prefs"
        private const val KEY_INSTALLATION_ID = "installation_id"
    }

    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * يعيد بصمة الجهاز النهائية: 8 بايت بصيغة hex (16 حرفًا).
     * مثال: A1B2C3D4E5F60718
     */
    fun getDeviceFingerprint(): String {
        val androidId = getAndroidId()
        val installationId = getOrCreateInstallationId()
        val keystorePublicKey = getOrCreateKeystorePublicKeyBase64()

        val combined = "$androidId|$installationId|$keystorePublicKey"
        val hash = MessageDigest.getInstance("SHA-256").digest(combined.toByteArray())
        val first8Bytes = hash.take(8).toByteArray()

        return first8Bytes.joinToString("") { "%02x".format(it) }.uppercase()
    }

    /**
     * يعيد البصمة بصيغة مقروءة مع شرطات: A1B2-C3D4-E5F6-0718
     */
    fun getFormattedFingerprint(): String {
        val fp = getDeviceFingerprint()
        if (fp.length != 16) return fp
        return "${fp.substring(0, 4)}-${fp.substring(4, 8)}-${fp.substring(8, 12)}-${fp.substring(12, 16)}"
    }

    private fun getAndroidId(): String {
        return try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }
    }

    private fun getOrCreateInstallationId(): String {
        var id = prefs.getString(KEY_INSTALLATION_ID, null)
        if (id.isNullOrBlank()) {
            id = UUID.randomUUID().toString()
            prefs.edit().putString(KEY_INSTALLATION_ID, id).apply()
        }
        return id
    }

    private fun getOrCreateKeystorePublicKeyBase64(): String {
        return try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

            if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
                val generator = KeyPairGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_RSA,
                    "AndroidKeyStore"
                )
                generator.initialize(
                    KeyGenParameterSpec.Builder(
                        KEYSTORE_ALIAS,
                        KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
                    )
                        .setDigests(KeyProperties.DIGEST_SHA256)
                        .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                        .build()
                )
                generator.generateKeyPair()
            }

            val publicKey: PublicKey = keyStore.getCertificate(KEYSTORE_ALIAS).publicKey
            val encoded = publicKey.encoded
            Base64.encodeToString(encoded, Base64.NO_WRAP)
        } catch (e: Exception) {
            "no-keystore-key"
        }
    }
}