package com.daftar.v2.ui.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daftar.v2.data.db.dao.ExpenseCategoryDao
import com.daftar.v2.data.entity.ExpenseCategoryEntity
import com.daftar.v2.data.template.ExpenseTemplate
import com.daftar.v2.data.template.ExpenseTemplateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExpenseTemplatesState(
    val loading: Boolean = true,
    val templates: List<ExpenseTemplate> = emptyList(),
    val categories: List<ExpenseCategoryEntity> = emptyList(),
    val message: String? = null,
    val isError: Boolean = false
)

@HiltViewModel
class ExpenseTemplatesViewModel @Inject constructor(
    private val templateManager: ExpenseTemplateManager,
    private val categoryDao: ExpenseCategoryDao
) : ViewModel() {

    private val _state = MutableStateFlow(ExpenseTemplatesState())
    val state: StateFlow<ExpenseTemplatesState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            try {
                val templates = templateManager.getAll()
                val cats = categoryDao.getAll()
                _state.value = ExpenseTemplatesState(
                    loading = false,
                    templates = templates,
                    categories = cats
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    loading = false,
                    message = e.message ?: "خطأ في تحميل القوالب",
                    isError = true
                )
            }
        }
    }

    fun addTemplate(
        name: String,
        amountCents: Long,
        currency: String,
        categoryId: Long?,
        note: String
    ) {
        viewModelScope.launch {
            try {
                templateManager.add(
                    name = name,
                    defaultAmountCents = amountCents,
                    currency = currency,
                    categoryId = categoryId,
                    note = note
                )
                _state.value = _state.value.copy(
                    message = "تم إضافة القالب",
                    isError = false
                )
                load()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    message = e.message ?: "خطأ في الإضافة",
                    isError = true
                )
            }
        }
    }

    fun updateTemplate(template: ExpenseTemplate) {
        viewModelScope.launch {
            try {
                templateManager.update(template)
                _state.value = _state.value.copy(
                    message = "تم تحديث القالب",
                    isError = false
                )
                load()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    message = e.message ?: "خطأ في التحديث",
                    isError = true
                )
            }
        }
    }

    fun deleteTemplate(templateId: String) {
        viewModelScope.launch {
            try {
                templateManager.delete(templateId)
                _state.value = _state.value.copy(
                    message = "تم حذف القالب",
                    isError = false
                )
                load()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    message = e.message ?: "خطأ في الحذف",
                    isError = true
                )
            }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}