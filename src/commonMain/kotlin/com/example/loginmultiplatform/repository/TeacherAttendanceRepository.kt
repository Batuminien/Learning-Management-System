package com.example.loginmultiplatform.repository

import com.example.loginmultiplatform.model.AttendanceResponse
import com.example.loginmultiplatform.model.CourseStatisticsResponse
import com.example.loginmultiplatform.model.TeacherCourseResponse
import com.example.loginmultiplatform.model.ResponseWrapper
import com.example.loginmultiplatform.model.StudentCourseResponse
import com.example.loginmultiplatform.model.TeacherAttendanceRequest
import com.example.loginmultiplatform.model.TeacherClassResponse
import com.example.loginmultiplatform.network.ApiClient
import com.example.loginmultiplatform.network.ApiService

class TeacherAttendanceRepository {
    private val apiService = ApiClient.retrofit.create(ApiService::class.java)

    suspend fun getTeacherClasses(): ResponseWrapper<List<TeacherClassResponse>> {
        val response = apiService.fetchTeacherClasses()

        if (response.success) {
            return response
        } else {
            throw Exception(response.message)
        }
    }

    suspend fun getTeacherCourses(teacherId: Int): ResponseWrapper<List<TeacherCourseResponse>> {
        val response = apiService.fetchTeacherCourses(teacherId)

        if (response.success) {
            return response
        } else {
            throw Exception(response.message)
        }
    }

    suspend fun getCourseStatistics(courseId: Int, classId: Int, startDate: String, endDate: String): ResponseWrapper<List<CourseStatisticsResponse>> {
        val response = apiService.fetchCourseStatistics(courseId, classId, startDate, endDate)

        if (response.success) {
            return response
        } else {
            throw Exception(response.message)
        }
    }

    suspend fun saveAttendanceBulk(attendanceList: List<TeacherAttendanceRequest>): ResponseWrapper<Int> {
        val response = apiService.saveAttendanceBulk(attendanceList)

        if (response.success) {
            return response
        } else {
            throw Exception(response.message)
        }
    }

    suspend fun getAllCourses(): ResponseWrapper<List<StudentCourseResponse>> {
        val response = apiService.fetchAllCourses()

        if (response.success) {
            return response
        } else {
            throw Exception(response.message)
        }
    }

    suspend fun getAllClasses(): ResponseWrapper<List<TeacherClassResponse>> {
        val response = apiService.fetchAllClasses()

        if (response.success) {
            return response
        } else {
            throw Exception(response.message)
        }
    }

    suspend fun updateAttendance(attendanceId: Int, attendanceUpdateRequest: TeacherAttendanceRequest) : ResponseWrapper<AttendanceResponse> {
        val response = apiService.updateAttendance(attendanceId, attendanceUpdateRequest)

        if(response.success) {
            return response
        } else {
            throw Exception(response.message)
        }
    }
}

