package com.example.loginmultiplatform.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loginmultiplatform.model.LoginData
import com.example.loginmultiplatform.model.StudentInfoResponse
import com.example.loginmultiplatform.network.TokenManager
import com.example.loginmultiplatform.repository.ProfilePhotoRepository
import com.example.loginmultiplatform.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class ProfilePhotoViewModel : ViewModel() {
    private val repository = ProfilePhotoRepository()

    private val _profilePhotoUrl = MutableStateFlow<String?>(null)
    val profilePhotoUrl: StateFlow<String?> = _profilePhotoUrl

    private val _studentInfo = MutableStateFlow<StudentInfoResponse?>(null)
    val studentInfo: StateFlow<StudentInfoResponse?> = _studentInfo

    fun fetchProfilePhoto(userId: Int) {
        viewModelScope.launch {
            try {
                val photoData = repository.fetchProfilePhoto(userId)
                _profilePhotoUrl.value = photoData.photoUrl
            } catch (e: Exception) {
                e.printStackTrace()
                _profilePhotoUrl.value = "default_url"
            }
        }
    }

    fun fetchStudentInfo(studentId: Int) {
        viewModelScope.launch {
            try {
                val studentInfos = repository.fetchStudentInfo(studentId)
                _studentInfo.value = studentInfos
            } catch (e: Exception) {
                e.printStackTrace()
                _studentInfo.value = null
            }
        }
    }

    fun uploadPp(file: MultipartBody.Part) {
        viewModelScope.launch {
            try {
                val response = repository.uploadPp(file)
                _profilePhotoUrl.value = response.photoUrl
            } catch (e: Exception) {
                e.printStackTrace()
                _profilePhotoUrl.value = "default_url"
            }
        }
    }
}