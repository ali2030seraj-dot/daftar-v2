package com.daftar.v2.ui.branding

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daftar.v2.branding.BrandingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class BrandingUiState(
    val loading: Boolean = true,
    val storeName: String = "",
    val storePhone: String = "",
    val storeAddress: String = "",
    val logoBitmap: Bitmap? = null,
    val message: String? = null,
    val isError: Boolean = false
)

@HiltViewModel
class BrandingViewModel @Inject constructor(
    private val brandingManager: BrandingManager
) : ViewModel() {

    private val _state = MutableStateFlow(BrandingUiState())
    val state: StateFlow<BrandingUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            try {
                _state.value = _state.value.copy(
                    loading = false,
                    storeName = brandingManager.getStoreName(),
                    storePhone = brandingManager.getStorePhone(),
                    storeAddress = brandingManager.getStoreAddress(),
                    logoBitmap = brandingManager.getLogoBitmap()
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    loading = false,
                    message = e.message ?: "خطأ في التحميل",
                    isError = true
                )
            }
        }
    }

    fun saveAll(name: String, phone: String, address: String) {
        viewModelScope.launch {
            try {
                brandingManager.setStoreName(name)
                brandingManager.setStorePhone(phone)
                brandingManager.setStoreAddress(address)

                _state.value = _state.value.copy(
                    storeName = name,
                    storePhone = phone,
                    storeAddress = address,
                    message = "✅ تم حفظ البيانات",
                    isError = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    message = "فشل الحفظ: ${e.message}",
                    isError = true
                )
            }
        }
    }

    fun setStoreName(name: String) {
        viewModelScope.launch {
            try {
                brandingManager.setStoreName(name)
                _state.value = _state.value.copy(storeName = name)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    message = "فشل الحفظ",
                    isError = true
                )
            }
        }
    }

    fun setStorePhone(phone: String) {
        viewModelScope.launch {
            try {
                brandingManager.setStorePhone(phone)
                _state.value = _state.value.copy(storePhone = phone)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    message = "فشل الحفظ",
                    isError = true
                )
            }
        }
    }

    fun setStoreAddress(address: String) {
        viewModelScope.launch {
            try {
                brandingManager.setStoreAddress(address)
                _state.value = _state.value.copy(storeAddress = address)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    message = "فشل الحفظ",
                    isError = true
                )
            }
        }
    }

    fun uploadLogo(uri: Uri) {
        viewModelScope.launch {
            _state.value = _state.value.copy(message = null)
            val success: Boolean = brandingManager.saveLogo(uri)
            if (success) {
                _state.value = _state.value.copy(
                    logoBitmap = brandingManager.getLogoBitmap(),
                    message = "✅ تم حفظ الشعار",
                    isError = false
                )
            } else {
                _state.value = _state.value.copy(
                    message = "❌ فشل حفظ الشعار",
                    isError = true
                )
            }
        }
    }

    fun deleteLogo() {
        viewModelScope.launch {
            try {
                brandingManager.deleteLogo()
                _state.value = _state.value.copy(
                    logoBitmap = null,
                    message = "تم حذف الشعار",
                    isError = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    message = "فشل الحذف",
                    isError = true
                )
            }
        }
    }

    // ═══════════════════════════════════════════════════════
    //              ⭐⭐⭐ حفظ تعديلات الشعار
    // ═══════════════════════════════════════════════════════

    /**
     * يحفظ الشعار بعد التعديل (تدوير + تكبير + إزاحة).
     *
     * @param rotation  الزاوية بالدرجات
     * @param scale     التكبير (0.5f - 4f)
     * @param offsetX   الإزاحة الأفقية (قيمة افتراضية = 0)
     * @param offsetY   الإزاحة الرأسية (قيمة افتراضية = 0)
     */
    fun saveTransformedLogo(
        rotation: Float,
        scale: Float,
        offsetX: Float = 0f,
        offsetY: Float = 0f
    ) {
        viewModelScope.launch {
            try {
                val source: Bitmap? = _state.value.logoBitmap
                if (source == null) return@launch

                _state.value = _state.value.copy(loading = true)

                val transformed: Bitmap = withContext(Dispatchers.IO) {
                    applyTransforms(source, rotation, scale, offsetX, offsetY)
                }

                val success: Boolean = withContext(Dispatchers.IO) {
                    brandingManager.saveLogoBitmap(transformed)
                }

                _state.value = _state.value.copy(
                    loading = false,
                    logoBitmap = if (success) transformed else source,
                    message = if (success) "✅ تم حفظ تعديلات الشعار" else "❌ فشل الحفظ",
                    isError = !success
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    loading = false,
                    message = "❌ خطأ: ${e.message}",
                    isError = true
                )
            }
        }
    }

    /**
     * يطبق التحويلات على الصورة: تدوير + تكبير + إزاحة.
     */
    private fun applyTransforms(
        source: Bitmap,
        rotation: Float,
        scale: Float,
        offsetX: Float,
        offsetY: Float
    ): Bitmap {
        val outputSize: Int = 512
        val result: Bitmap = Bitmap.createBitmap(
            outputSize,
            outputSize,
            Bitmap.Config.ARGB_8888
        )
        val canvas: Canvas = Canvas(result)
        canvas.drawColor(android.graphics.Color.TRANSPARENT)

        val paint: Paint = Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
            isDither = true
        }

        // نحسب fitScale لتملأ الصورة المربع
        val srcWidth: Float = source.width.toFloat()
        val srcHeight: Float = source.height.toFloat()
        val outSize: Float = outputSize.toFloat()
        val scaleX: Float = outSize / srcWidth
        val scaleY: Float = outSize / srcHeight
        val fitScale: Float = if (scaleX > scaleY) scaleX else scaleY
        val totalScale: Float = fitScale * scale

        // ⭐ المصفوفة مع الإزاحة
        val matrix: Matrix = Matrix().apply {
            postTranslate(-srcWidth / 2f, -srcHeight / 2f)
            postRotate(rotation)
            postScale(totalScale, totalScale)
            postTranslate(
                outSize / 2f + offsetX,
                outSize / 2f + offsetY
            )
        }

        canvas.drawBitmap(source, matrix, paint)
        return result
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}