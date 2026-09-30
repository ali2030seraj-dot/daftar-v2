package com.daftar.v2.security.license

import javax.inject.Inject
import javax.inject.Singleton

/**
 * يفك تشفير الكود إلى LicensePayload.
 *
 * بنية الكود:
 *   SA1-XXXXX-XXXXX-...-XXXXX
 *
 * بنية البايتات:
 *   [1]    version
 *   [1]    plan
 *   [4]    serial (big-endian)
 *   [4]    issuedAt (big-endian)
 *   [4]    expiresAt (big-endian)
 *   [8]    deviceFingerprint
 *   [64]   signature
 *
 * المجموع: 86 بايت
 */
@Singleton
class LicenseCodec @Inject constructor() {

    companion object {
        private const val PREFIX = "SA1-"
        private const val SIGNATURE_LENGTH = 64
        private const val PAYLOAD_LENGTH = 22
        private const val TOTAL_LENGTH = 86
    }

    /**
     * يفك تشفير الكود ويعيد LicensePayload.
     * يرمي استثناءً إذا كان الكود غير صالح.
     */
    fun decode(code: String): LicensePayload {
        val trimmed = code.trim()
        require(trimmed.startsWith(PREFIX)) { "الكود يجب أن يبدأ بـ SA1-" }

        val raw = trimmed.removePrefix(PREFIX).replace("-", "")
        require(raw.isNotEmpty()) { "الكود فارغ" }

        val bytes = try {
            Base58.decode(raw)
        } catch (e: Exception) {
            throw IllegalArgumentException("الكود يحتوي على أحرف غير صالحة", e)
        }

        require(bytes.size == TOTAL_LENGTH) {
            "طول الكود غير صحيح. المتوقع: $TOTAL_LENGTH، الفعلي: ${bytes.size}"
        }

        val payload = bytes.copyOfRange(0, PAYLOAD_LENGTH)
        val signature = bytes.copyOfRange(PAYLOAD_LENGTH, TOTAL_LENGTH)

        var offset = 0

        val version = payload[offset++].toInt() and 0xFF
        val planByte = payload[offset++].toInt() and 0xFF

        val serial = readInt(payload, offset); offset += 4
        val issuedAt = readInt(payload, offset); offset += 4
        val expiresAt = readInt(payload, offset); offset += 4

        val fpBytes = payload.copyOfRange(offset, offset + 8); offset += 8
        val deviceFingerprint = fpBytes.joinToString("") { "%02X".format(it) }

        return LicensePayload(
            version = version,
            plan = LicensePlan.fromByte(planByte),
            serial = serial,
            issuedAt = issuedAt,
            expiresAt = expiresAt,
            deviceFingerprint = deviceFingerprint,
            signature = signature,
            rawPayload = payload
        )
    }

    private fun readInt(bytes: ByteArray, offset: Int): Long {
        return ((bytes[offset].toLong() and 0xFF) shl 24) or
                ((bytes[offset + 1].toLong() and 0xFF) shl 16) or
                ((bytes[offset + 2].toLong() and 0xFF) shl 8) or
                (bytes[offset + 3].toLong() and 0xFF)
    }
}