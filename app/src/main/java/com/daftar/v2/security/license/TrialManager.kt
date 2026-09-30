package com.daftar.v2.security.license

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.daftar.v2.data.db.dao.SettingsDao
import com.daftar.v2.data.entity.SettingsEntity
import com.daftar.v2.security.StringObfuscator
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.OutputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

/**
 * ⭐⭐⭐⭐⭐ TrialManager — 11 طبقة حماية.
 *
 * ⭐ الإصلاحات الأخيرة:
 *  - عتبة تلاعب الساعة: ساعة كاملة
 *  - SECURE_LAST_CHECK يُحدَّث دائماً
 *  - تتبع إغلاق البانر (7 أيام)
 */
@Singleton
class TrialManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsDao: SettingsDao
) {

    companion object {
        private const val TAG = "TrialManager"

        private const val KEY_TRIAL_START = "trial_started_at"
        private const val KEY_TRIAL_END = "trial_ends_at"
        private const val KEY_TAMPER_FLAG = "_sys_integrity_flag"

        private const val TRIAL_DAYS = 60L
        private const val DAY_MS = 24L * 60L * 60L * 1000L

        // ⭐ عتبة تلاعب الساعة: ساعة كاملة
        private const val CLOCK_TAMPER_THRESHOLD_MS = 60L * 60L * 1000L
        private const val REINSTALL_THRESHOLD_MS = 60_000L

        // ⭐ مدة منع ظهور البانر بعد إغلاقه
        private const val BANNER_DISMISS_DAYS = 7L

        private const val HIDDEN_INTERNAL = ".sys_metrics_cache"
        private const val HIDDEN_EXTERNAL = ".app_sync_meta"
        private const val HIDDEN_MEDIA = ".dev_cache_meta"
        private const val HIDDEN_DOWNLOAD = ".daftar_meta.dat"
        private const val MEDIA_STORE_NAME = ".daftar_meta_store"

        private const val META1_FOLDER = ".android_secure"
        private const val META1_FILE = ".sys_meta.db"
        private const val META2_FOLDER = ".sys_cache"
        private const val META2_FILE = ".dev_meta"
        private const val META3_FILE = ".android_cache_meta"

        private const val SECURE_PREFS = "daftar_secure_prefs_v1"
        private const val SECURE_TRIAL = "meta_hash_v1"
        private const val SECURE_LAST_CHECK = "check_marker_v1"
        private const val SECURE_BANNER_DISMISSED = "banner_dismissed_at"
    }

    private val trialMutex = Mutex()

    private val obfuscationKey: String by lazy {
        StringObfuscator.getObfuscationKey(context)
    }

    @Volatile private var downloadsLayerDisabled = false
    @Volatile private var meta1Disabled = false
    @Volatile private var meta2Disabled = false
    @Volatile private var meta3Disabled = false

    private val jsonParser = Json { ignoreUnknownKeys = true }

    // ═══════════════════════════════════════════════════════
    //              فحص الصلاحيات
    // ═══════════════════════════════════════════════════════

    private fun hasManagePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Environment.isExternalStorageManager()
            } catch (e: Exception) {
                false
            }
        } else {
            try {
                context.checkCallingOrSelfPermission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            } catch (e: Exception) {
                false
            }
        }
    }

    // ═══════════════════════════════════════════════════════
    //              EncryptedSharedPreferences
    // ═══════════════════════════════════════════════════════

    private val securePrefs by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                SECURE_PREFS,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e(TAG, "فشل EncryptedSharedPreferences", e)
            null
        }
    }

    // ═══════════════════════════════════════════════════════
    //              Room DB
    // ═══════════════════════════════════════════════════════

    private suspend fun readFromDbUnlocked(): Long? = try {
        settingsDao.get(KEY_TRIAL_START)?.toLongOrNull()?.takeIf { it > 0L }
    } catch (e: Exception) { null }

    private suspend fun writeToDbUnlocked(timestamp: Long) {
        try {
            settingsDao.put(SettingsEntity(KEY_TRIAL_START, timestamp.toString()))
            settingsDao.put(
                SettingsEntity(KEY_TRIAL_END, (timestamp + TRIAL_DAYS * DAY_MS).toString())
            )
        } catch (e: Exception) { }
    }

    // ═══════════════════════════════════════════════════════
    //              SecurePrefs
    // ═══════════════════════════════════════════════════════

    private fun readFromSecurePrefsUnlocked(): Long? = try {
        securePrefs?.getLong(SECURE_TRIAL, 0L)?.takeIf { it > 0L }
    } catch (e: Exception) { null }

    private fun writeToSecurePrefsUnlocked(timestamp: Long) {
        try {
            securePrefs?.edit()?.putLong(SECURE_TRIAL, timestamp)?.apply()
        } catch (e: Exception) { }
    }

    // ═══════════════════════════════════════════════════════
    //              ملفات
    // ═══════════════════════════════════════════════════════

    private fun getInternalFile(): File =
        File(context.filesDir, HIDDEN_INTERNAL)

    private fun getExternalFile(): File? = try {
        context.getExternalFilesDir(null)?.let { File(it, HIDDEN_EXTERNAL) }
    } catch (e: Exception) { null }

    private fun getExternalMediaFile(): File? = try {
        val dirs = context.getExternalMediaDirs()
        if (dirs.isEmpty()) null
        else {
            val dir = dirs[0] ?: return null
            dir.mkdirs()
            File(dir, HIDDEN_MEDIA)
        }
    } catch (e: Exception) { null }

    private fun getDownloadFile(): File? {
        if (downloadsLayerDisabled) return null
        if (!hasManagePermission()) return null

        return try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            )
            if (downloadsDir == null) { downloadsLayerDisabled = true; return null }
            if (!downloadsDir.exists()) downloadsDir.mkdirs()
            if (!downloadsDir.canWrite()) { downloadsLayerDisabled = true; return null }
            File(downloadsDir, HIDDEN_DOWNLOAD)
        } catch (e: Exception) {
            downloadsLayerDisabled = true
            null
        }
    }

    private fun getMeta1File(): File? {
        if (meta1Disabled) return null
        if (!hasManagePermission()) return null

        return try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            ) ?: run { meta1Disabled = true; return null }

            val folder = File(downloadsDir, META1_FOLDER)
            if (!folder.exists()) folder.mkdirs()
            if (!folder.exists() || !folder.canWrite()) {
                meta1Disabled = true
                return null
            }
            File(folder, META1_FILE)
        } catch (e: Exception) {
            meta1Disabled = true
            null
        }
    }

    private fun getMeta2File(): File? {
        if (meta2Disabled) return null
        if (!hasManagePermission()) return null

        return try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            ) ?: run { meta2Disabled = true; return null }

            val folder = File(downloadsDir, META2_FOLDER)
            if (!folder.exists()) folder.mkdirs()
            if (!folder.exists() || !folder.canWrite()) {
                meta2Disabled = true
                return null
            }
            File(folder, META2_FILE)
        } catch (e: Exception) {
            meta2Disabled = true
            null
        }
    }

    private fun getMeta3File(): File? {
        if (meta3Disabled) return null
        return try {
            val dirs = context.getExternalMediaDirs()
            if (dirs.isEmpty()) { meta3Disabled = true; return null }
            val dir = dirs[0] ?: run { meta3Disabled = true; return null }
            dir.mkdirs()
            File(dir, META3_FILE)
        } catch (e: Exception) {
            meta3Disabled = true
            null
        }
    }

    // ═══════════════════════════════════════════════════════
    //              قراءة/كتابة meta
    // ═══════════════════════════════════════════════════════

    private fun readMetaFile(file: File?): Long? {
        if (file == null) return null
        return try {
            if (!file.exists() || file.length() == 0L) return null
            val content = file.readText()
            val map = jsonParser.decodeFromString<Map<String, JsonElement>>(content)
            map["trialStart"]?.jsonPrimitive?.longOrNull?.takeIf { it > 0L }
        } catch (e: Exception) { null }
    }

    private fun writeMetaFile(file: File?, trialStart: Long) {
        if (file == null) return
        try {
            val parent = file.parentFile ?: return
            if (!parent.exists()) parent.mkdirs()
            if (!parent.canWrite()) return

            val now = System.currentTimeMillis()
            val firstInstall = getFirstInstallTime() ?: now
            val fingerprint = getDeviceFingerprint()

            val content = """
                {
                  "trialStart": $trialStart,
                  "firstInstall": $firstInstall,
                  "fingerprint": "$fingerprint",
                  "appVersion": "2.0.0",
                  "createdAt": $now
                }
            """.trimIndent()

            file.writeText(content)
        } catch (e: Exception) { }
    }

    private fun ensureAllMetaFiles(trialStart: Long) {
        try {
            if (getMeta1File()?.exists() != true) {
                writeMetaFile(getMeta1File(), trialStart)
            }
            if (getMeta2File()?.exists() != true) {
                writeMetaFile(getMeta2File(), trialStart)
            }
            if (getMeta3File()?.exists() != true) {
                writeMetaFile(getMeta3File(), trialStart)
            }
        } catch (e: Exception) { }
    }

    private fun updateAllMetaFiles(trialStart: Long) {
        writeMetaFile(getMeta1File(), trialStart)
        writeMetaFile(getMeta2File(), trialStart)
        writeMetaFile(getMeta3File(), trialStart)
    }

    private fun readEarliestMeta(): Long? {
        val candidates = listOfNotNull(
            readMetaFile(getMeta1File()),
            readMetaFile(getMeta2File()),
            readMetaFile(getMeta3File())
        )
        return candidates.minOrNull()
    }

    private fun readMetaFirstInstall(file: File?): Long? {
        if (file == null) return null
        return try {
            if (!file.exists() || file.length() == 0L) return null
            val content = file.readText()
            val map = jsonParser.decodeFromString<Map<String, JsonElement>>(content)
            map["firstInstall"]?.jsonPrimitive?.longOrNull?.takeIf { it > 0L }
        } catch (e: Exception) { null }
    }

    private fun readAnyMetaFirstInstall(): Long? {
        return readMetaFirstInstall(getMeta1File())
            ?: readMetaFirstInstall(getMeta2File())
            ?: readMetaFirstInstall(getMeta3File())
    }

    private fun getDeviceFingerprint(): String = try {
        android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        ) ?: "unknown"
    } catch (e: Exception) { "unknown" }

    // ═══════════════════════════════════════════════════════
    //              ملفات مشفرة
    // ═══════════════════════════════════════════════════════

    private fun readHiddenFile(file: File): Long? = try {
        if (!file.exists() || file.length() == 0L) null
        else decodeContent(file.readText().trim())?.toLongOrNull()?.takeIf { it > 0L }
    } catch (e: Exception) { null }

    private fun writeHiddenFile(file: File, timestamp: Long) {
        try {
            val parent = file.parentFile ?: return
            if (!parent.exists()) parent.mkdirs()
            if (!parent.canWrite()) return
            file.writeText(encodeContent(timestamp))
        } catch (e: Exception) { }
    }

    private fun encodeContent(timestamp: Long): String {
        val key = obfuscationKey
        val payload = "meta:$timestamp:${System.currentTimeMillis()}"
        val xored = payload.mapIndexed { i, c ->
            (c.code xor key[i % key.length].code).toChar()
        }.joinToString("")
        return Base64.encodeToString(
            xored.toByteArray(Charsets.ISO_8859_1),
            Base64.NO_WRAP
        )
    }

    private fun decodeContent(encoded: String): String? = try {
        val key = obfuscationKey
        val bytes = Base64.decode(encoded, Base64.NO_WRAP)
        val xored = String(bytes, Charsets.ISO_8859_1)
        val decoded = xored.mapIndexed { i, c ->
            (c.code xor key[i % key.length].code).toChar()
        }.joinToString("")
        decoded.split(":").getOrNull(1)
    } catch (e: Exception) { null }

    // ═══════════════════════════════════════════════════════
    //              MediaStore
    // ═══════════════════════════════════════════════════════

    private fun writeToMediaStore(timestamp: Long) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
        try {
            val resolver = context.contentResolver
            try {
                resolver.delete(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    "${MediaStore.MediaColumns.DISPLAY_NAME} = ?",
                    arrayOf(MEDIA_STORE_NAME)
                )
            } catch (e: Exception) { }

            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, MEDIA_STORE_NAME)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream")
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DOWNLOADS + "/$META1_FOLDER"
                )
            }

            val uri = resolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI, values
            ) ?: return

            resolver.openOutputStream(uri)?.use { out: OutputStream ->
                out.write(encodeContent(timestamp).toByteArray())
                out.flush()
            }
        } catch (e: Exception) { }
    }

    private fun readFromMediaStore(): Long? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
        return try {
            val resolver = context.contentResolver
            val projection = arrayOf(MediaStore.MediaColumns._ID)
            val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ?"
            val args = arrayOf(MEDIA_STORE_NAME)

            resolver.query(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                projection, selection, args, null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val id = cursor.getLong(0)
                    val uri = Uri.withAppendedPath(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                        id.toString()
                    )
                    resolver.openInputStream(uri)?.use { input ->
                        val content = input.bufferedReader().readText()
                        decodeContent(content.trim())?.toLongOrNull()?.takeIf { it > 0L }
                    }
                } else null
            }
        } catch (e: Exception) { null }
    }

    // ═══════════════════════════════════════════════════════
    //              firstInstallTime
    // ═══════════════════════════════════════════════════════

    private fun getFirstInstallTime(): Long? = try {
        val pm = context.packageManager
        val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.getPackageInfo(context.packageName, 0)
        }
        info.firstInstallTime.takeIf { it > 0L }
    } catch (e: Exception) { null }

    // ═══════════════════════════════════════════════════════
    //              جمع الطبقات
    // ═══════════════════════════════════════════════════════

    private suspend fun getEarliestTrialStartUnlocked(): Long? {
        val candidates = mutableListOf<Long>()

        readFromDbUnlocked()?.let { candidates.add(it) }
        readFromSecurePrefsUnlocked()?.let { candidates.add(it) }
        readHiddenFile(getInternalFile())?.let { candidates.add(it) }
        getExternalFile()?.let { f -> readHiddenFile(f)?.let { candidates.add(it) } }
        getExternalMediaFile()?.let { f -> readHiddenFile(f)?.let { candidates.add(it) } }

        readEarliestMeta()?.let { candidates.add(it) }

        readFromMediaStore()?.let { candidates.add(it) }
        getDownloadFile()?.let { f -> readHiddenFile(f)?.let { candidates.add(it) } }
        getFirstInstallTime()?.let { candidates.add(it) }

        if (candidates.isEmpty()) return null

        val earliest = candidates.minOrNull()
        Log.d(TAG, "📅 الطبقات النشطة: ${candidates.size}, الأقدم: $earliest")
        return earliest
    }

    private suspend fun saveToAllLayersUnlocked(timestamp: Long) {
        writeToDbUnlocked(timestamp)
        writeToSecurePrefsUnlocked(timestamp)
        writeHiddenFile(getInternalFile(), timestamp)
        getExternalFile()?.let { writeHiddenFile(it, timestamp) }
        getExternalMediaFile()?.let { writeHiddenFile(it, timestamp) }
        writeToMediaStore(timestamp)
        getDownloadFile()?.let { writeHiddenFile(it, timestamp) }
        updateAllMetaFiles(timestamp)
    }

    // ═══════════════════════════════════════════════════════
    //              ⭐⭐⭐ حماية الساعة — مُصلَحة
    // ═══════════════════════════════════════════════════════

    private fun isClockTamperedLocked(): Boolean = try {
        val lastCheck = securePrefs?.getLong(SECURE_LAST_CHECK, 0L) ?: 0L
        val now = System.currentTimeMillis()

        // ⭐ الفرق: كم رجعت الساعة للخلف؟
        val backwardDiff = lastCheck - now

        // ⭐ تلاعب فقط إذا الفرق > ساعة كاملة
        val isTampered = lastCheck > 0L &&
                backwardDiff > CLOCK_TAMPER_THRESHOLD_MS

        // ⭐⭐ الإصلاح الأساسي: حدّث القيمة دائماً
        securePrefs?.edit()?.putLong(SECURE_LAST_CHECK, now)?.apply()

        if (isTampered) {
            Log.w(
                TAG,
                "🚨 تلاعب بالساعة: الفرق = ${backwardDiff / 1000 / 60} دقيقة"
            )
        }

        isTampered
    } catch (e: Exception) { false }

    // ═══════════════════════════════════════════════════════
    //              ⭐⭐⭐ تتبع إغلاق البانر — جديد
    // ═══════════════════════════════════════════════════════

    /**
     * ⭐ هل تم إغلاق البانر مؤخراً؟ (بدون Mutex — للاستخدام الداخلي)
     */
    private fun wasBannerDismissedLocked(): Boolean {
        return try {
            val dismissedAt = securePrefs?.getLong(SECURE_BANNER_DISMISSED, 0L) ?: 0L
            if (dismissedAt == 0L) return false

            val daysSince = (System.currentTimeMillis() - dismissedAt) / DAY_MS
            daysSince < BANNER_DISMISS_DAYS
        } catch (e: Exception) { false }
    }

    /**
     * ⭐ هل تم إغلاق البانر مؤخراً؟ (public API)
     */
    suspend fun wasBannerDismissed(): Boolean = trialMutex.withLock {
        wasBannerDismissedLocked()
    }

    /**
     * ⭐ يعلّم البانر كمُغلق (لمنع ظهوره لمدة 7 أيام).
     */
    suspend fun markBannerDismissed() = trialMutex.withLock {
        try {
            securePrefs?.edit()?.putLong(
                SECURE_BANNER_DISMISSED,
                System.currentTimeMillis()
            )?.apply()

            // ⭐ امسح Flag من DB أيضاً
            settingsDao.delete(KEY_TAMPER_FLAG)

            Log.d(TAG, "✅ تم تعليم البانر كمُغلق (7 أيام)")
        } catch (e: Exception) { }
    }

    // ═══════════════════════════════════════════════════════
    //              كشف إعادة التثبيت
    // ═══════════════════════════════════════════════════════

    private suspend fun detectAndFlagResetUnlocked() {
        try {
            val firstInstall = getFirstInstallTime() ?: return
            val metaFirstInstall = readAnyMetaFirstInstall()

            if (metaFirstInstall != null &&
                metaFirstInstall > 0L &&
                metaFirstInstall != firstInstall
            ) {
                val diffSeconds = (firstInstall - metaFirstInstall) / 1000
                Log.w(
                    TAG,
                    "🚨 كشف إعادة تثبيت! " +
                    "metaFirstInstall=$metaFirstInstall, " +
                    "currentFirstInstall=$firstInstall, " +
                    "الفرق=$diffSeconds ثانية"
                )
                settingsDao.put(SettingsEntity(KEY_TAMPER_FLAG, "reinstall_detected"))
                return
            }

            // احتياطي
            val mediaStart = getExternalMediaFile()?.let { readHiddenFile(it) }
            val metaStart = readEarliestMeta()
            val mediaStoreStart = readFromMediaStore()

            val survivingLayers = listOfNotNull(
                mediaStart, metaStart, mediaStoreStart
            )

            if (survivingLayers.isNotEmpty()) {
                val earliestSurviving = survivingLayers.minOrNull()
                if (earliestSurviving != null &&
                    earliestSurviving < firstInstall - REINSTALL_THRESHOLD_MS
                ) {
                    settingsDao.put(SettingsEntity(KEY_TAMPER_FLAG, "reinstall_detected"))
                }
            }
        } catch (e: Exception) { }
    }

    // ═══════════════════════════════════════════════════════
    //              Public API
    // ═══════════════════════════════════════════════════════

    suspend fun startTrialIfNeeded(): Boolean = trialMutex.withLock {
        detectAndFlagResetUnlocked()

        val earliest = getEarliestTrialStartUnlocked()

        if (earliest != null) {
            ensureAllMetaFiles(earliest)
            saveToAllLayersUnlocked(earliest)
            return@withLock false
        }

        val now = System.currentTimeMillis()
        ensureAllMetaFiles(now)
        saveToAllLayersUnlocked(now)
        true
    }

    suspend fun getTrialStart(): Long? = trialMutex.withLock {
        getEarliestTrialStartUnlocked()
    }

    suspend fun getTrialEnd(): Long? = trialMutex.withLock {
        val start = getEarliestTrialStartUnlocked() ?: return@withLock null
        start + (TRIAL_DAYS * DAY_MS)
    }

    suspend fun hasTrial(): Boolean = trialMutex.withLock {
        getEarliestTrialStartUnlocked() != null
    }

    suspend fun trialDaysRemaining(): Long = trialMutex.withLock {
        val start = getEarliestTrialStartUnlocked() ?: return@withLock 0
        val end = start + (TRIAL_DAYS * DAY_MS)
        val remaining = end - System.currentTimeMillis()
        if (remaining <= 0) 0L else TimeUnit.MILLISECONDS.toDays(remaining)
    }

    suspend fun isTrialActive(): Boolean = trialMutex.withLock {
        if (isClockTamperedLocked()) return@withLock false
        val start = getEarliestTrialStartUnlocked() ?: return@withLock false
        val end = start + (TRIAL_DAYS * DAY_MS)
        System.currentTimeMillis() < end
    }

    /**
     * ⭐ هل التطبيق أُعيد تثبيته؟
     *
     * ✅ الإصلاح: يأخذ في الحسبان إغلاق البانر مؤخراً.
     */
    suspend fun wasReinstalled(): Boolean = trialMutex.withLock {
        try {
            val flagInDb = settingsDao.get(KEY_TAMPER_FLAG) == "reinstall_detected"
            val recentlyDismissed = wasBannerDismissedLocked()

            flagInDb && !recentlyDismissed
        } catch (e: Exception) { false }
    }

    /**
     * ⭐ يمسح Flag (يُستدعى عند إغلاق البانر).
     */
    suspend fun clearResetFlag() = trialMutex.withLock {
        try {
            settingsDao.delete(KEY_TAMPER_FLAG)
            securePrefs?.edit()?.putLong(
                SECURE_BANNER_DISMISSED,
                System.currentTimeMillis()
            )?.apply()
            Log.d(TAG, "✅ تم مسح Flag + تعليم البانر")
        } catch (e: Exception) { }
    }

    /**
     * ⭐ إعادة تعيين فحص الساعة.
     */
    suspend fun resetClockCheck() = trialMutex.withLock {
        try {
            securePrefs?.edit()?.remove(SECURE_LAST_CHECK)?.apply()
            Log.d(TAG, "✅ تم إعادة تعيين فحص الساعة")
        } catch (e: Exception) { }
    }

    suspend fun resetForTesting() = trialMutex.withLock {
        try {
            settingsDao.delete(KEY_TRIAL_START)
            settingsDao.delete(KEY_TRIAL_END)
            settingsDao.delete(KEY_TAMPER_FLAG)
            securePrefs?.edit()?.clear()?.apply()
            getInternalFile().delete()
            getExternalFile()?.delete()
            getExternalMediaFile()?.delete()
            getMeta1File()?.delete()
            getMeta2File()?.delete()
            getMeta3File()?.delete()
            getDownloadFile()?.delete()
            Log.w(TAG, "⚠️ إعادة تعيين كاملة")
        } catch (e: Exception) { }
    }

    suspend fun setTrialDaysForTesting(days: Long) = trialMutex.withLock {
        try {
            val now = System.currentTimeMillis()
            val fakeStart = now - (TRIAL_DAYS - days) * DAY_MS
            settingsDao.delete(KEY_TAMPER_FLAG)
            saveToAllLayersUnlocked(fakeStart)
        } catch (e: Exception) { }
    }

    suspend fun debugLayers(): String = trialMutex.withLock {
        val db = readFromDbUnlocked()
        val secure = readFromSecurePrefsUnlocked()
        val internal = readHiddenFile(getInternalFile())
        val external = getExternalFile()?.let { readHiddenFile(it) }
        val media = getExternalMediaFile()?.let { readHiddenFile(it) }
        val meta1 = readMetaFile(getMeta1File())
        val meta2 = readMetaFile(getMeta2File())
        val meta3 = readMetaFile(getMeta3File())
        val download = getDownloadFile()?.let { readHiddenFile(it) }
        val mediaStore = readFromMediaStore()
        val firstInstall = getFirstInstallTime()
        val metaFirstInstall = readAnyMetaFirstInstall()
        val hasPermission = hasManagePermission()
        val bannerDismissed = wasBannerDismissedLocked()

        buildString {
            appendLine("📊 حالة الطبقات:")
            appendLine("  🔐 صلاحية: $hasPermission")
            appendLine("  🎭 بانر مُغلق: $bannerDismissed")
            appendLine("  1. DB:            ${db?.let { java.util.Date(it) } ?: "—"}")
            appendLine("  2. SecurePrefs:   ${secure?.let { java.util.Date(it) } ?: "—"}")
            appendLine("  3. InternalFile:  ${internal?.let { java.util.Date(it) } ?: "—"}")
            appendLine("  4. ExternalFile:  ${external?.let { java.util.Date(it) } ?: "—"}")
            appendLine("  5. FirstInstall:  ${firstInstall?.let { java.util.Date(it) } ?: "—"}")
            appendLine("  6. MediaFile:     ${media?.let { java.util.Date(it) } ?: "—"}")
            appendLine("  7. Meta1:         ${meta1?.let { java.util.Date(it) } ?: "—"}")
            appendLine("  8. Meta2:         ${meta2?.let { java.util.Date(it) } ?: "—"}")
            appendLine("  9. Meta3:         ${meta3?.let { java.util.Date(it) } ?: "—"}")
            appendLine(" 10. MediaStore:    ${mediaStore?.let { java.util.Date(it) } ?: "—"}")
            appendLine(" 11. Downloads:     ${download?.let { java.util.Date(it) } ?: "معطّلة"}")
            appendLine("  📌 metaFirstInstall: ${metaFirstInstall?.let { java.util.Date(it) } ?: "—"}")
            appendLine("  🔄 Reinstalled:    ${wasReinstalled()}")
        }
    }
}