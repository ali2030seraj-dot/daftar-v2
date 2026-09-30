package com.daftar.v2.branding

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.daftar.v2.data.db.dao.SettingsDao
import com.daftar.v2.data.entity.SettingsEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * يدير شعار المتجر وبيانات العلامة التجارية.
 */
@Singleton
class BrandingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsDao: SettingsDao
) {

    companion object {
        private const val KEY_STORE_NAME = "store_name"
        private const val KEY_STORE_PHONE = "store_phone"
        private const val KEY_STORE_ADDRESS = "store_address"
        private const val LOGO_DIR = "branding"
        private const val LOGO_FILE = "logo.png"
    }

    // ============ اسم المتجر ============

    suspend fun getStoreName(): String =
        settingsDao.get(KEY_STORE_NAME) ?: "دفتر حساباتي"

    suspend fun setStoreName(name: String) {
        settingsDao.put(SettingsEntity(KEY_STORE_NAME, name.trim()))
    }

    // ============ هاتف المتجر ============

    suspend fun getStorePhone(): String =
        settingsDao.get(KEY_STORE_PHONE) ?: ""

    suspend fun setStorePhone(phone: String) {
        settingsDao.put(SettingsEntity(KEY_STORE_PHONE, phone.trim()))
    }

    // ============ عنوان المتجر ============

    suspend fun getStoreAddress(): String =
        settingsDao.get(KEY_STORE_ADDRESS) ?: ""

    suspend fun setStoreAddress(address: String) {
        settingsDao.put(SettingsEntity(KEY_STORE_ADDRESS, address.trim()))
    }

    // ============ الشعار ============

    /**
     * يُعيد ملف الشعار إذا وُجد.
     */
    fun getLogoFile(): File? {
        val file = File(File(context.filesDir, LOGO_DIR), LOGO_FILE)
        return if (file.exists()) file else null
    }

    /**
     * يُعيد Bitmap للشعار إذا وُجد.
     */
    fun getLogoBitmap(): Bitmap? {
        val file = getLogoFile() ?: return null
        return try {
            BitmapFactory.decodeFile(file.absolutePath)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * يحفظ الشعار من URI (يُصغّر تلقائياً).
     */
    suspend fun saveLogo(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext false

            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (bitmap == null) return@withContext false

            return@withContext saveLogoBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * ⭐⭐⭐ يحفظ Bitmap للشعار مباشرة (يُستخدم بعد تعديل الصورة).
     * - يُصغّر إلى 512×512
     * - يحفظ كـ PNG
     */
    suspend fun saveLogoBitmap(bitmap: Bitmap): Boolean = withContext(Dispatchers.IO) {
        try {
            val resized: Bitmap = resizeBitmap(bitmap, 512)

            val dir: File = File(context.filesDir, LOGO_DIR).apply { mkdirs() }
            val outFile: File = File(dir, LOGO_FILE)

            FileOutputStream(outFile).use { out ->
                resized.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * يحذف الشعار.
     */
    suspend fun deleteLogo() {
        getLogoFile()?.delete()
    }

    /**
     * يُصغّر الصورة مع الحفاظ على النسبة.
     */
    private fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        val w: Int = bitmap.width
        val h: Int = bitmap.height
        if (w <= maxSize && h <= maxSize) return bitmap

        val ratio: Float = w.toFloat() / h.toFloat()
        val newW: Int
        val newH: Int
        if (w > h) {
            newW = maxSize
            newH = (maxSize / ratio).toInt()
        } else {
            newH = maxSize
            newW = (maxSize * ratio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, newW, newH, true)
    }
}