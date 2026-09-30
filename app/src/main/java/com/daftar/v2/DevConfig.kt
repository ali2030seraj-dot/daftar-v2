package com.daftar.v2

/**
 * إعدادات المطور.
 *
 * ⚠️ قبل إصدار النسخة النهائية:
 * - غيّر BYPASS_LICENSE إلى false.
 * - غيّر BYPASS_APP_LOCK إلى false (إن أردت قفل التطبيق).
 */
object DevConfig {
    /**
     * تعطيل فحص الترخيص مؤقتًا.
     * true  → لا يُفحص الترخيص (للتطوير).
     * false → الترخيص يعمل طبيعيًا (للإنتاج).
     */
    const val BYPASS_LICENSE = false 

    /**
     * تعطيل قفل التطبيق (AppLock) مؤقتًا.
     */
    const val BYPASS_APP_LOCK = false
}