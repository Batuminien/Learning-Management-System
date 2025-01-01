package com.example.loginmultiplatform.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.loginmultiplatform.model.StudentAnnouncementResponse
import com.example.loginmultiplatform.repository.AdministratorsAnnouncementsRepository
import com.example.loginmultiplatform.repository.StudentAnnouncementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdministratorAnnouncementsViewModel : ViewModel() {
    private val repository = AdministratorsAnnouncementsRepository()

    private val _announcement = MutableStateFlow<StudentAnnouncementResponse?>(null)
    val announcement: StateFlow<StudentAnnouncementResponse?> = _announcement

    private val _saveOperationStatus = MutableStateFlow<String?>(null)
    val saveOperationStatus: StateFlow<String?> = _saveOperationStatus

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun saveAnnouncements(announcementRequest: StudentAnnouncementResponse) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val announcementResponse = repository.saveAnnouncements(announcementRequest)
                _announcement.value = announcementResponse
                _saveOperationStatus.value = "Duyuru başarılı bir şekilde eklendi!"
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error occured"
                _saveOperationStatus.value = "İşlem sırasında hata oluştu!"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun notifyAnnouncement(announcementId: Int) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                repository.notifyAnnouncement(announcementId)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error occured"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteAnnouncement(announcementId: Int) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                repository.deleteAnnouncement(announcementId)
                _saveOperationStatus.value = "Duyuru başarılı bir şekilde silindi!"
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error occured"
                _saveOperationStatus.value = "İşlem sırasında hata oluştu!"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun readAnnouncement(announcementId: Int) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                repository.readAnnouncement(announcementId)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error occured"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateAnnouncement(announcementId: Int, attendance: StudentAnnouncementResponse) {
        viewModelScope.launch {
            try {
                repository.updateAnnouncement(announcementId, attendance)
                _saveOperationStatus.value = "Duyuru başarılı bir şekilde güncellendi!"
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error occured"
                _saveOperationStatus.value = "İşlem sırasında bir hata oluştu!"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearSaveOperationStatus() {
        _saveOperationStatus.value = null
    }
}