package com.example.loginmultiplatform.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loginmultiplatform.model.AttendanceResponse
import com.example.loginmultiplatform.model.CourseStatisticsResponse
import com.example.loginmultiplatform.model.ResponseWrapper
import com.example.loginmultiplatform.model.StudentCourseResponse
import com.example.loginmultiplatform.model.TeacherAttendanceRequest
import com.example.loginmultiplatform.model.TeacherClassResponse
import com.example.loginmultiplatform.repository.TeacherAttendanceRepository
import com.example.loginmultiplatform.model.TeacherCourseResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class TeacherAttendanceViewModel : ViewModel() {

    private val repository = TeacherAttendanceRepository()

    private val _teacherClasses = MutableStateFlow<List<TeacherClassResponse>>(emptyList())
    val teacherClasses: StateFlow<List<TeacherClassResponse>> = _teacherClasses

    private val _courseId = MutableStateFlow<List<TeacherCourseResponse>>(emptyList())
    val courseId: StateFlow<List<TeacherCourseResponse>> = _courseId

    private val _courseStats = MutableStateFlow<List<CourseStatisticsResponse>>(emptyList())
    val courseStats: StateFlow<List<CourseStatisticsResponse>> = _courseStats

    private val _allCourse = MutableStateFlow<List<StudentCourseResponse>>(emptyList())
    val allCourse: StateFlow<List<StudentCourseResponse>> = _allCourse

    private val _allClass = MutableStateFlow<List<TeacherClassResponse>>(emptyList())
    val allClass: StateFlow<List<TeacherClassResponse>> = _allClass

    private val _isSaving = MutableStateFlow(false)
    //val isSaving: StateFlow<Boolean> = _isSaving

    private val _saveResult = MutableStateFlow<ResponseWrapper<Int>?>(null)
    //val saveResult: StateFlow<ResponseWrapper<Int>?> = _saveResult

    private val _saveError = MutableStateFlow<String?>(null)
    //val saveError: StateFlow<String?> = _saveError

    private val _errorMessage = MutableStateFlow<String?>(null)
    //val errorMessage: StateFlow<String?> = _errorMessage

    private val _isLoading = MutableStateFlow(false)
    //val isLoading: StateFlow<Boolean> = _isLoading

    private val _bulkOperationStatus = MutableStateFlow<String?>(null)
    val bulkOperationStatus: StateFlow<String?> = _bulkOperationStatus

    private val _updateAttendanceResponse = MutableStateFlow<AttendanceResponse?>(null)
    //val updateAttendanceResponse: StateFlow<AttendanceResponse?> = _updateAttendanceResponse

    fun fetchTeacherClasses() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val classes = repository.getTeacherClasses()
                if (classes.success) {
                    _teacherClasses.value = classes.data
                } else {
                    _errorMessage.value = classes.message
                }
            } catch (e: Exception) {
                _errorMessage.value = "Bir hata oluştu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchTeacherCourses(teacherId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val courses = repository.getTeacherCourses(teacherId)
                if(courses.success) {
                    _courseId.value = courses.data
                } else {
                    _errorMessage.value = courses.message
                }
            } catch (e: Exception) {
                _errorMessage.value = "Bir hata oluştu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchCourseStatistics(courseId: Int, classId: Int, startDate: String, endDate: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val courseStat = repository.getCourseStatistics(courseId, classId, startDate, endDate)
                if(courseStat.success) {
                    _courseStats.value = _courseStats.value + courseStat.data
                } else {
                    _errorMessage.value = courseStat.message
                }
            } catch (e: Exception) {
                _errorMessage.value = "Bir hata oluştu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveAttendanceBulk(attendanceList: List<TeacherAttendanceRequest>) {
        viewModelScope.launch {
            _isSaving.value = true
            _saveError.value = null

            try {
                val response = repository.saveAttendanceBulk(attendanceList)
                _saveResult.value = response
                _bulkOperationStatus.value = "İşlem başarılı bir şekilde tamamlandı!"
            } catch (e: HttpException) {
                _saveError.value = "Bir hata oluştu: ${e.message()}"
                _bulkOperationStatus.value = "İşlem sırasında hata oluştu!"
            } catch (e: Exception) {
                _saveError.value = "Bir hata oluştu: ${e.localizedMessage}"
                _bulkOperationStatus.value = "İşlem sırasında hata oluştu!"
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun fetchAllCourses() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val allCourses = repository.getAllCourses()
                if(allCourses.success) {
                    _allCourse.value = allCourses.data
                } else {
                    _errorMessage.value = allCourses.message
                }
            } catch (e: Exception) {
                _errorMessage.value = "Bir hata oluştu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchAllClasses() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val allClass = repository.getAllClasses()
                if(allClass.success) {
                    _allClass.value = allClass.data
                } else {
                    _errorMessage.value = allClass.message
                }
            } catch (e: Exception) {
                _errorMessage.value = "Bir hata oluştu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateAttendance(
            attendanceId: Int,
            studentId: Int,
            date: String,
            status: String,
            comment: String,
            classId: Int,
            courseId: Int
        ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val attendanceRequest = TeacherAttendanceRequest(
                    studentId = studentId,
                    date = date,
                    status = status,
                    comment = comment,
                    classId = classId,
                    courseId = courseId
                )
                val response = repository.updateAttendance(attendanceId, attendanceRequest)

                if(response.success) {
                    _updateAttendanceResponse.value = response.data
                    _bulkOperationStatus.value = "Güncelleme başarılı!"
                } else {
                    _errorMessage.value = response.message
                    _bulkOperationStatus.value = "Güncelleme sırasında hata oluştu!"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Bir hata oluştu: ${e.message}"
                _bulkOperationStatus.value = "Güncelleme sırasında hata oluştu!"
            } finally {
                _isSaving.value = false
            }
        }
    }

}
