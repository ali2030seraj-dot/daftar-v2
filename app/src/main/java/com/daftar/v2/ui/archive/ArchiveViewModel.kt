package com.daftar.v2.ui.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daftar.v2.data.entity.PersonEntity
import com.daftar.v2.data.repository.PersonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    private val personRepository: PersonRepository
) : ViewModel() {

    private val _people = MutableStateFlow<List<PersonEntity>>(emptyList())
    val people: StateFlow<List<PersonEntity>> = _people.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        viewModelScope.launch {
            personRepository.observeArchived().collect {
                _people.value = it
            }
        }
    }

    fun restore(id: Long) {
        viewModelScope.launch {
            try {
                personRepository.restore(id)
                _message.value = "تمت الاستعادة"
            } catch (e: Exception) {
                _message.value = e.message
            }
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            try {
                personRepository.hardDelete(id)
                _message.value = "تم الحذف النهائي"
            } catch (e: Exception) {
                _message.value = e.message
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}