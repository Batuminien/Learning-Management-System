package com.example.loginmultiplatform.repository

import com.example.loginmultiplatform.network.ApiClient
import com.example.loginmultiplatform.model.AttendanceResponse
import com.example.loginmultiplatform.model.AttendanceStats
import com.example.loginmultiplatform.model.StudentCourseResponse
import com.example.loginmultiplatform.network.ApiService

class AttendanceRepository {
    private val apiService = ApiClient.retrofit.create(ApiService::class.java)

    suspend fun fetchAttendance(
        studentId: Int,
        startDate: String,
        endDate: String
    ): List<AttendanceResponse> {
        //println("StudentId in attendance: $studentId")
        val response = apiService.getAttendance(studentId, startDate, endDate)

        if (response.success) {
            return response.data
        } else {
            throw Exception(response.message)
        }
    }

    suspend fun fetchAttendanceSingular(
        studentId: Int,
        startDate: String,
        endDate: String
    ): List<AttendanceResponse> {
        //println("StudentId in attendance: $studentId")
        val response = apiService.getAttendance(studentId, startDate, endDate)

        if (response.success) {
            return response.data
        } else {
            throw Exception(response.message)
        }
    }

    suspend fun getStudentCourses(studentId: Int): List<StudentCourseResponse> {
        val response = apiService.getStudentCourses(studentId)

        if (response.success) {
            return response.data
        } else {
            throw Exception(response.message)
        }
    }

    suspend fun fetchAttendanceStats(
        studentId: Int,
        classId: Int
    ): List<AttendanceStats> {

        val response = apiService.getAttendanceStats(studentId, classId)

        if (response.success) {
            return response.data
        } else {
            throw Exception(response.message)
        }
        //println("StudentId in attendance stats: $studentId, ClassId: $classId")
        //return apiService.getAttendanceStats(studentId, classId)
    }
}