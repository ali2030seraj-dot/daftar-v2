package com.daftar.v2.pdf

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daftar.v2.branding.BrandingManager
import com.daftar.v2.domain.usecase.GetStatementUseCase
import com.daftar.v2.domain.usecase.Statement
import com.daftar.v2.util.MoneyFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

data class PdfUiState(
    val busy: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false
)

@HiltViewModel
class PdfViewModel @Inject constructor(
    private val getStatement: GetStatementUseCase,
    private val pdfGenerator: StatementPdfGenerator,
    private val shareHelper: WhatsAppShareHelper,
    private val brandingManager: BrandingManager
) : ViewModel() {

    private val _state = MutableStateFlow(PdfUiState())
    val state: StateFlow<PdfUiState> = _state.asStateFlow()

    /**
     * ✅ يقرأ بيانات العلامة التجارية من BrandingManager.
     */
    private suspend fun getBranding(): PdfBranding {
        return PdfBranding(
            logoBitmap = brandingManager.getLogoBitmap(),
            storeName = brandingManager.getStoreName(),
            storePhone = brandingManager.getStorePhone(),
            storeAddress = brandingManager.getStoreAddress()
        )
    }

    fun savePdfToDownloads(context: Context, personId: Long) {
        viewModelScope.launch {
            _state.value = PdfUiState(busy = true)
            try {
                val stmt: Statement? = getStatement(personId)
                if (stmt == null) {
                    _state.value = PdfUiState(
                        message = "الحساب غير موجود",
                        isError = true
                    )
                    return@launch
                }

                val branding = getBranding()

                val result: PdfStorageHelper.SaveResult = withContext(Dispatchers.IO) {
                    val tempFile: File = pdfGenerator.generate(context, stmt, branding)
                    // ⭐ إصلاح: person قد يكون null
                    val fileName: String = PdfStorageHelper.buildFileName(
                        stmt.person?.name ?: "statement"
                    )
                    PdfStorageHelper.savePdfToDownloads(context, tempFile, fileName)
                }

                _state.value = if (result.success) {
                    PdfUiState(
                        message = "تم الحفظ في ${result.location}",
                        isError = false
                    )
                } else {
                    PdfUiState(
                        message = result.error ?: "فشل الحفظ",
                        isError = true
                    )
                }
            } catch (e: Exception) {
                _state.value = PdfUiState(
                    message = e.message ?: "فشل الحفظ",
                    isError = true
                )
            }
        }
    }

    fun sharePdf(context: Context, personId: Long) {
        viewModelScope.launch {
            _state.value = PdfUiState(busy = true)
            try {
                val stmt: Statement? = getStatement(personId)
                if (stmt == null) {
                    _state.value = PdfUiState(
                        message = "الحساب غير موجود",
                        isError = true
                    )
                    return@launch
                }

                val branding = getBranding()

                val file: File = withContext(Dispatchers.IO) {
                    pdfGenerator.generate(context, stmt, branding)
                }

                val message: String = buildMessage(stmt)
                shareHelper.sharePdf(
                    context = context,
                    file = file,
                    // ⭐ إصلاح: person قد يكون null
                    phone = stmt.person?.phone,
                    message = message
                )

                _state.value = PdfUiState()
            } catch (e: Exception) {
                _state.value = PdfUiState(
                    message = e.message ?: "فشل المشاركة",
                    isError = true
                )
            }
        }
    }

    private fun buildMessage(stmt: Statement): String {
        // ⭐ إصلاح: person قد يكون null
        val currency: String = stmt.person?.currency ?: stmt.displayCurrency
        val personName: String = stmt.person?.name ?: "العميل"
        return buildString {
            appendLine("مرحبًا $personName،")
            appendLine("كشف حسابكم:")
            appendLine("إجمالي المبالغ: ${MoneyFormatter.format(stmt.totalDebtCents, currency)}")
            appendLine("إجمالي السداد: ${MoneyFormatter.format(stmt.totalPaymentCents, currency)}")
            appendLine("الرصيد المتبقي: ${MoneyFormatter.format(stmt.finalBalanceCents, currency)}")
            appendLine()
            appendLine("شكرًا لتعاملكم معنا.")
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}