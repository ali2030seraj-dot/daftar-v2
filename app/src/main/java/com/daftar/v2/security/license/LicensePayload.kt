package com.daftar.v2.security.license

/**
 * يمثل بيانات الترخيص المُستخرجة من الكود.
 */
data class LicensePayload(
    val version: Int,
    val plan: LicensePlan,
    val serial: Long,
    val issuedAt: Long,
    val expiresAt: Long,      // صفر للدائم
    val deviceFingerprint: String,
    val signature: ByteArray,
    val rawPayload: ByteArray   // البايتات الأصلية للتحقق
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LicensePayload) return false
        return version == other.version &&
                plan == other.plan &&
                serial == other.serial &&
                issuedAt == other.issuedAt &&
                expiresAt == other.expiresAt &&
                deviceFingerprint == other.deviceFingerprint
    }

    override fun hashCode(): Int {
        var result = version
        result = 31 * result + plan.hashCode()
        result = 31 * result + serial.hashCode()
        result = 31 * result + issuedAt.hashCode()
        result = 31 * result + expiresAt.hashCode()
        result = 31 * result + deviceFingerprint.hashCode()
        return result
    }
}

enum class LicensePlan {
    TRIAL,
    M30,
    M90,
    M365,
    LIFETIME;

    companion object {
        fun fromByte(b: Int): LicensePlan = when (b) {
            0 -> TRIAL
            1 -> M30
            2 -> M90
            3 -> M365
            4 -> LIFETIME
            else -> M30
        }

        fun toDisplayName(plan: LicensePlan): String = when (plan) {
            TRIAL -> "تجريبي (30 يومًا)"
            M30 -> "شهري (30 يومًا)"
            M90 -> "3 أشهر (90 يومًا)"
            M365 -> "سنوي (365 يومًا)"
            LIFETIME -> "دائم"
        }

        fun toDays(plan: LicensePlan): Long = when (plan) {
            TRIAL -> 30L
            M30 -> 30L
            M90 -> 90L
            M365 -> 365L
            LIFETIME -> 0L
        }
    }
}