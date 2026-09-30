package com.daftar.v2.ui.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daftar.v2.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoreViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    fun deleteAllTransactions() {
        viewModelScope.launch {
            try {
                transactionRepository.deleteAllTransactions()
            } catch (e: Exception) {
                android.util.Log.e("MORE_VM", "فشل حذف العمليات", e)
            }
        }
    }
}