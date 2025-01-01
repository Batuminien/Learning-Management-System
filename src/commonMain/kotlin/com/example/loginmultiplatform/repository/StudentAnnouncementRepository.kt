package com.example.loginmultiplatform.repository

import com.example.loginmultiplatform.network.ApiClient
import com.example.loginmultiplatform.model.StudentAnnouncementResponse
import com.example.loginmultiplatform.model.StudentClassResponse
import com.example.loginmultiplatform.network.ApiService

class StudentAnnouncementRepository {
    private val apiService = ApiClient.retrofit.create(ApiService::class.java)

    suspend fun fetchAnnouncementsByClassId(
        classId: Int
    ): List<StudentAnnouncementResponse> {
        val response = apiService.fetchAnnouncementsByClassId(classId)

        if (response.success) {
            return response.data
        } else {
            throw Exception(response.message)
        }
    }

    suspend fun fetchStudentClass(
        studentId: Int
    ): StudentClassResponse {
        val response = apiService.getStudentClass(studentId)

        if(response.success) {
            return response.data
        } else {
            throw Exception(response.message)
        }
    }
}