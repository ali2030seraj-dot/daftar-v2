package com.daftar.v2.security.license

import android.util.Log
import net.i2p.crypto.eddsa.EdDSAEngine
import net.i2p.crypto.eddsa.EdDSAPublicKey
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable
import net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * يتحقق من توقيع Ed25519 على Payload الترخيص.
 *
 * يستخدم مكتبة net.i2p.crypto:eddsa (Java خالصة) لضمان التوافق
 * مع كل إصدارات Android، بغض النظر عن دعم النظام المدمج.
 */
@Singleton
class LicenseValidator @Inject constructor(
    private val publicKeyProvider: PublicKeyProvider
) {

    companion object {
        private const val TAG = "LicenseValidator"
    }

    fun verifySignature(payload: LicensePayload): Boolean {
        return try {
            val rawPubBytes = publicKeyProvider.getRawPublicKey()
            if (rawPubBytes == null) {
                Log.e(TAG, "❌ Public key is null")
                return false
            }

            Log.d(TAG, "Raw public key size: ${rawPubBytes.size}")

            // بناء المفتاح العام Ed25519
            val curve = EdDSANamedCurveTable.getByName("Ed25519")
            val pubKeySpec = EdDSAPublicKeySpec(rawPubBytes, curve)
            val publicKey = EdDSAPublicKey(pubKeySpec)

            // محرك التحقق
            val engine = EdDSAEngine(MessageDigest.getInstance("SHA-512"))
            engine.initVerify(publicKey)
            engine.update(payload.rawPayload)

            val result = engine.verify(payload.signature)
            Log.d(TAG, "═══════════════════════════")
            Log.d(TAG, "Payload size: ${payload.rawPayload.size}")
            Log.d(TAG, "Signature size: ${payload.signature.size}")
            Log.d(TAG, "Verify result: $result")
            Log.d(TAG, "═══════════════════════════")

            result
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception: ${e.javaClass.simpleName}: ${e.message}", e)
            e.printStackTrace()
            false
        }
    }

    fun verify(payload: LicensePayload): ValidationOutcome {
        if (payload.version != 1) {
            return ValidationOutcome.InvalidVersion(payload.version)
        }
        if (!verifySignature(payload)) {
            return ValidationOutcome.InvalidSignature
        }
        return ValidationOutcome.Valid
    }
}

sealed class ValidationOutcome {
    object Valid : ValidationOutcome()
    data class InvalidVersion(val version: Int) : ValidationOutcome()
    object InvalidSignature : ValidationOutcome()
}