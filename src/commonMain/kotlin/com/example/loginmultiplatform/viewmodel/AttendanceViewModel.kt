package com.example.loginmultiplatform.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loginmultiplatform.model.AttendanceResponse
import com.example.loginmultiplatform.model.AttendanceStats
import com.example.loginmultiplatform.model.ResponseWrapper
import com.example.loginmultiplatform.model.StudentCourseResponse
import com.example.loginmultiplatform.repository.AttendanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AttendanceViewModel : ViewModel() {
    private val repository = AttendanceRepository()

    private val _attendanceMap = MutableStateFlow<Map<Int, List<AttendanceResponse>>>(emptyMap())
    val attendanceMap: StateFlow<Map<Int, List<AttendanceResponse>>> = _attendanceMap

    private val _attendanceStatsMap = MutableStateFlow<Map<Pair<Int, Int>, List<AttendanceStats>>>(
        emptyMap()
    )
    val attendanceStatsMap: StateFlow<Map<Pair<Int, Int>, List<AttendanceStats>>> = _attendanceStatsMap

    private val _studentCoursesMap = MutableStateFlow<Map<Int, List<StudentCourseResponse>>>(
        emptyMap()
    )
    val studentCoursesMap: StateFlow<Map<Int, List<StudentCourseResponse>>> = _studentCoursesMap

    private val _attendanceList = MutableStateFlow<List<AttendanceResponse>>(emptyList())
    val attendanceList: StateFlow<List<AttendanceResponse>> = _attendanceList

    private val _attendanceStats = MutableStateFlow<List<AttendanceStats>>(emptyList())
    val attendanceStats: StateFlow<List<AttendanceStats>> = _attendanceStats

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _studentCourses = MutableStateFlow<List<StudentCourseResponse>>(emptyList())
    val studentCourses: StateFlow<List<StudentCourseResponse>> = _studentCourses

    fun fetchAttendance(studentId: Int, startDate: String, endDate: String) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val attendance = repository.fetchAttendance(studentId, startDate, endDate)
                _attendanceList.value = attendance

                val currentMap = _attendanceMap.value.toMutableMap()
                currentMap[studentId] = attendance
                _attendanceMap.value = currentMap
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error occured"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchStudentCourses(studentId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = repository.getStudentCourses(studentId)
                _studentCourses.value = response

                val currentMap = _studentCoursesMap.value.toMutableMap()
                currentMap[studentId] = response
                _studentCoursesMap.value = currentMap

                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchAttendanceStats(studentId: Int, classId: Int) {

        _errorMessage.value = null

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val stats = repository.fetchAttendanceStats(studentId, classId)
                _attendanceStats.value = stats

                val currentMap = _attendanceStatsMap.value.toMutableMap()
                currentMap[studentId to classId] = stats
                _attendanceStatsMap.value = currentMap

            } catch (e: Exception) {
                _errorMessage.value = "Bir hata oluştu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}