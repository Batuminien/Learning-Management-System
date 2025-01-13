package com.example.loginmultiplatform.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.example.loginmultiplatform.model.CoordinatorInfoResponse
import com.example.loginmultiplatform.model.ProfilePhotoResponse
import com.example.loginmultiplatform.network.ApiClient
import com.example.loginmultiplatform.model.StudentInfoResponse
import com.example.loginmultiplatform.model.TeacherInfoResponse
import com.example.loginmultiplatform.network.ApiService
import okhttp3.MultipartBody
import retrofit2.http.Multipart

fun ByteArray.toBitmap(): Bitmap {
    return BitmapFactory.decodeByteArray(this, 0, this.size)
}


class ProfilePhotoRepository {
    private val apiService = ApiClient.retrofit.create(ApiService::class.java)

    suspend fun fetchProfilePhoto(
        userId: Int
    ): ByteArray {
        val response = apiService.fetchProfilePhoto(userId)

        if (response.isSuccessful) {
            val responseBody = response.body()
            if (responseBody != null) {
                return responseBody.bytes()
            } else {
                throw Exception("Response body is null")
            }
        } else {
            throw Exception("Failed to fetch profile photo: ${response.message()}")
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

    suspend fun fetchCoordinatorInfo(
        coordinatorId: Int
    ): CoordinatorInfoResponse {
        val response = apiService.fetchCoordinatorInfo(coordinatorId)

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
            throw Exception("Upload Failed: ${response.message}")
        }
    }
}