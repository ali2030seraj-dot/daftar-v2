package com.daftar.v2.ui.receipt

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daftar.v2.branding.BrandingManager
import com.daftar.v2.data.db.dao.PersonDao
import com.daftar.v2.data.db.dao.TransactionDao
import com.daftar.v2.data.entity.PersonEntity
import com.daftar.v2.data.entity.TransactionEntity
import com.daftar.v2.data.entity.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReceiptUiState(
    val loading: Boolean = true,
    val person: PersonEntity? = null,
    val transaction: TransactionEntity? = null,
    val balanceBeforeCents: Long = 0L,
    val balanceAfterCents: Long = 0L,
    val receiptNumber: String = "",
    val storeName: String = "دفتر حساباتي",
    val storePhone: String = "",
    val storeAddress: String = "",
    val logoBitmap: Bitmap? = null,
    val message: String? = null,
    val isError: Boolean = false
) {
    val isPayment: Boolean get() = transaction?.type == TransactionType.PAYMENT
    val isDebt: Boolean get() = transaction?.type == TransactionType.DEBT
}

@HiltViewModel
class ReceiptViewModel @Inject constructor(
    private val personDao: PersonDao,
    private val transactionDao: TransactionDao,
    private val brandingManager: BrandingManager
) : ViewModel() {

    private val _state = MutableStateFlow(ReceiptUiState())
    val state: StateFlow<ReceiptUiState> = _state.asStateFlow()

    fun load(personId: Long, transactionId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, message = null)
            try {
                val person = personDao.getById(personId)
                if (person == null) {
                    _state.value = _state.value.copy(
                        loading = false,
                        message = "لم يتم العثور على الحساب",
                        isError = true
                    )
                    return@launch
                }

                val tx = transactionDao.getById(transactionId)
                if (tx == null) {
                    _state.value = _state.value.copy(
                        loading = false,
                        message = "لم يتم العثور على العملية",
                        isError = true
                    )
                    return@launch
                }

                val balanceAfter = computeBalanceAfter(personId, tx.id)
                val balanceBefore = if (tx.type == TransactionType.DEBT) {
                    balanceAfter - tx.amountCents
                } else {
                    balanceAfter + tx.amountCents
                }

                // ✅ قراءة بيانات المتجر
                val storeName = brandingManager.getStoreName()
                val storePhone = brandingManager.getStorePhone()
                val storeAddress = brandingManager.getStoreAddress()
                val logo = brandingManager.getLogoBitmap()

                val year = java.util.Calendar.getInstance()
                    .get(java.util.Calendar.YEAR)
                val receiptNumber = "R-$year-${String.format("%04d", tx.id)}"

                _state.value = _state.value.copy(
                    loading = false,
                    person = person,
                    transaction = tx,
                    balanceBeforeCents = balanceBefore,
                    balanceAfterCents = balanceAfter,
                    receiptNumber = receiptNumber,
                    storeName = storeName,
                    storePhone = storePhone,
                    storeAddress = storeAddress,
                    logoBitmap = logo,
                    message = null,
                    isError = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    loading = false,
                    message = e.message ?: "خطأ في تحميل البيانات",
                    isError = true
                )
            }
        }
    }

    private suspend fun computeBalanceAfter(
        personId: Long,
        transactionId: Long
    ): Long {
        val all = transactionDao.getAll()
            .filter { it.personId == personId && !it.isVoided }
            .sortedBy { it.timestamp }

        var balance = 0L
        for (t in all) {
            balance += if (t.type == TransactionType.DEBT) {
                t.amountCents
            } else {
                -t.amountCents
            }
            if (t.id == transactionId) return balance
        }
        return balance
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}