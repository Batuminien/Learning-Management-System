package com.example.loginmultiplatform.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.loginmultiplatform.model.StudentAnnouncementResponse
import com.example.loginmultiplatform.repository.StudentAnnouncementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StudentAnnouncementViewModel : ViewModel() {
    private val repository = StudentAnnouncementRepository()

    private val _announcement = MutableStateFlow<List<StudentAnnouncementResponse>>(emptyList())
    val announcement: StateFlow<List<StudentAnnouncementResponse>> = _announcement

    private val _announcementUser = MutableStateFlow<List<StudentAnnouncementResponse>>(emptyList())
    val announcementUser : StateFlow<List<StudentAnnouncementResponse>> = _announcementUser

    private val _classId = MutableStateFlow<Int?>(null)
    val classId: StateFlow<Int?> = _classId

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun fetchAnnouncementsByClassId(classId: Int) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val announcementResponse = repository.fetchAnnouncementsByClassId(classId)
                _announcement.value = announcementResponse
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error occured"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getStudentClass(studentId: Int) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val response = repository.fetchStudentClass(studentId)
                _classId.value = response.id
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error occured"
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun getAnnouncementByUserId(userId: Int) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val announcementResponse = repository.getAnnouncementsUserId(userId)
                _announcementUser.value = announcementResponse
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error occured"
            } finally {
                _isLoading.value = false
            }
        }
    }
}