package com.example.loginmultiplatform.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loginmultiplatform.model.CoordinatorInfoResponse
import com.example.loginmultiplatform.model.LoginData
import com.example.loginmultiplatform.model.StudentInfoResponse
import com.example.loginmultiplatform.model.TeacherInfoResponse
import com.example.loginmultiplatform.network.TokenManager
import com.example.loginmultiplatform.repository.ProfilePhotoRepository
import com.example.loginmultiplatform.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class ProfilePhotoViewModel : ViewModel() {
    private val repository = ProfilePhotoRepository()

    private val _profilePhoto = MutableLiveData<Bitmap?>()
    val profilePhoto: LiveData<Bitmap?> = _profilePhoto

    private val _profilePhotoUrl = MutableLiveData<String?>()
    val profilePhotoUrl: LiveData<String?> = _profilePhotoUrl

    private val _coordinatorInfo = MutableStateFlow<CoordinatorInfoResponse?>(null)
    val coordinatorInfo: StateFlow<CoordinatorInfoResponse?> = _coordinatorInfo

    private val _studentInfo = MutableStateFlow<StudentInfoResponse?>(null)
    val studentInfo: StateFlow<StudentInfoResponse?> = _studentInfo

    private val _teacherInfo = MutableStateFlow<TeacherInfoResponse?>(null)
    val teacherInfo: StateFlow<TeacherInfoResponse?> = _teacherInfo

    fun fetchProfilePhoto(userId: Int) {
        viewModelScope.launch {
            try {
                val photoBytes = repository.fetchProfilePhoto(userId)
                val bitmap = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.size)
                _profilePhoto.postValue(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
                _profilePhoto.postValue(null)
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

    fun fetchTeacherInfo(teacherId: Int) {
        viewModelScope.launch {
            try {
                val teacherInfos = repository.fetchTeacherInfo(teacherId)
                _teacherInfo.value = teacherInfos
            } catch (e: Exception) {
                e.printStackTrace()
                _teacherInfo.value = null
            }
        }
    }

    fun fetchCoordinatorInfo(coordinatorId: Int) {
        viewModelScope.launch {
            try {
                val coordinatorInfos = repository.fetchCoordinatorInfo(coordinatorId)
                _coordinatorInfo.value = coordinatorInfos
            } catch (e: Exception) {
                e.printStackTrace()
                _coordinatorInfo.value = null
            }
        }
    }

    fun uploadPp(file: MultipartBody.Part, onUploadComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.uploadPp(file)

                if(response != null && response.photoUrl.isNotEmpty()) {
                    val newProfilePhotoUrl = response.photoUrl
                    _profilePhotoUrl.postValue(newProfilePhotoUrl)
                    onUploadComplete(true, "Fotoğraf başarılı bir şekilde güncellendi!")
                } else {
                    onUploadComplete(false, "Failed to upload profile photo: Invalid response.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onUploadComplete(false, "Fotoğraf yüklenirken hata oluştu!")
                _profilePhotoUrl.value = null
            }
        }
    }
}