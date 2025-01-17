package com.example.loginmultiplatform.viewmodel

import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loginmultiplatform.AppContext.context
import com.example.loginmultiplatform.model.ResponseWrapper
import com.example.loginmultiplatform.model.StudentAssignmentRequest
import com.example.loginmultiplatform.model.StudentAssignmentResponse
import com.example.loginmultiplatform.model.StudentClassResponse
import com.example.loginmultiplatform.model.StudentCourseResponse
import com.example.loginmultiplatform.model.StudentSubmission
import com.example.loginmultiplatform.repository.StudentHomeworkRepository
//import com.example.loginmultiplatform.ui.DocumentAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class StudentAssignmentViewModel : ViewModel() {

    private val repository = StudentHomeworkRepository()

    private val _studentAssignment = MutableStateFlow<List<StudentAssignmentResponse>>(emptyList())
    val studentAssignments : StateFlow<List<StudentAssignmentResponse>> = _studentAssignment

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isSaving = MutableStateFlow(false)

    private val _saveResult = MutableStateFlow<ResponseWrapper<Int>?>(null)

    private val _saveError = MutableStateFlow<String?>(null)

    private val _documentContent = MutableLiveData<String>()
    val documentContent: LiveData<String> get() = _documentContent


    fun fetchStudentAssignments(studentId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val assignments = repository.fetchStudentAssignments(studentId)

                if (assignments.success) {
                    _studentAssignment.value = assignments.data
                } else {
                    _errorMessage.value = assignments.message
                }
            } catch (e: Exception) {
                // Hata durumunda hem UI'ya mesaj veriyoruz, hem de loglama yapıyoruz
                _errorMessage.value = "Bir hata oluştu: ${e.message}"
                Log.e("fetchStudentAssignments", "Error: ${e.message}", e)  // Gelişmiş loglama
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun submitAssignment(assignmentId: Int, submission: StudentAssignmentRequest) {
        viewModelScope.launch {
            _isSaving.value = true
            _saveError.value = null

            try {
                val response = repository.submitAssignment(assignmentId, submission)
                if (response.success) {
                    // Ödev gönderme başarılı olduğunda yapılacak işlemler
                    // Ödev listesini güncellemek için başka bir işlem yapılabilir
                } else {
                    _saveError.value = response.message
                }
            } catch (e: Exception) {
                _saveError.value = "Error submitting assignment: ${e.message}"
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun unSubmitAssignment(assignmentId: Int) {
        viewModelScope.launch {
            _isSaving.value = true
            _saveError.value = null

            try {
                val response = repository.unSubmitAssignment(assignmentId)
                if (response.success) {
                    // Ödev geri alma başarılı olduğunda yapılacak işlemler
                    // Ödev listesini güncellemek için başka bir işlem yapılabilir
                } else {
                    _saveError.value = response.message
                }
            } catch (e: Exception) {
                _saveError.value = "Error un-submitting assignment: ${e.message}"
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun downloadTeacherDocument(documentId: Int) {
        viewModelScope.launch {
            val result = repository.downloadTeacherDocument(documentId)
            result.onSuccess { content ->
                _documentContent.value = content
            }.onFailure { error ->
                // Hata yönetimi
                Log.e("AssignmentViewModel", "Error downloading document", error)
            }
        }
    }

    private fun saveDocumentLocally(content: String, fileName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                resolver.openOutputStream(it)?.use { outputStream ->
                    outputStream.write(content.toByteArray())
                    outputStream.flush()
                }
            }
        } else {
            val file = File(Environment.getExternalStorageDirectory(), fileName)
            file.writeText(content)
        }
    }
    //    private fun saveDocumentLocally(content: String, fileName: String) {
//        val file = File(Environment.getExternalStorageDirectory(), fileName)
//        file.writeText(content)
//    }
    /*fun performDocumentAction(
        documentId: Long,
        action: DocumentAction,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                when (action) {
                    DocumentAction.OPEN -> {
                        val content = repository.downloadTeacherDocument(documentId.toInt())
                        onSuccess(content.toString())
                    }
                    DocumentAction.DOWNLOAD -> {
                        val content = repository.downloadTeacherDocument(documentId.toInt())
                        saveDocumentLocally(content.toString(), "downloaded_document_${documentId}.txt")
                        onSuccess("Document downloaded successfully!")
                    }
                    DocumentAction.REMOVE -> {
                        onSuccess("Document removed successfully!")
                    }
                }
            } catch (e: Exception) {
                onError(e)
            }
        }
    }*/
}