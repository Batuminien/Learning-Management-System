package com.example.loginmultiplatform.repository

import com.example.loginmultiplatform.model.ProfilePhotoResponse
import com.example.loginmultiplatform.network.ApiClient
import com.example.loginmultiplatform.model.StudentInfoResponse
import com.example.loginmultiplatform.model.TeacherInfoResponse
import com.example.loginmultiplatform.network.ApiService
import okhttp3.MultipartBody
import retrofit2.http.Multipart

class ProfilePhotoRepository {
    private val apiService = ApiClient.retrofit.create(ApiService::class.java)

    suspend fun fetchProfilePhoto(
        userId: Int
    ): ProfilePhotoResponse {
        val response = apiService.fetchProfilePhoto(userId)

        if (response.success) {
            return response.data
        } else {
            throw Exception(response.message)
        }
    }

    suspend fun fetchStudentInfo(
        studentId: Int
    ): StudentInfoResponse {
        val response = apiService.fetchStudentsInfo(studentId)

        if (response.success) {
            return response.data
        } else {
            throw Exception(response.message)
        }
    }

    suspend fun fetchTeacherInfo(
        teacherId: Int
    ): TeacherInfoResponse {
        val response = apiService.fetchTeacherInfo(teacherId)

        if (response.success) {
            return response.data
        } else {
            throw Exception(response.message)
        }
    }

    suspend fun uploadPp(
        file: MultipartBody.Part
    ): ProfilePhotoResponse {
        val response = apiService.uploadPp(file)

        if (response.success) {
            return response.data
        } else {
            throw Exception(response.message)
        }
    }
}