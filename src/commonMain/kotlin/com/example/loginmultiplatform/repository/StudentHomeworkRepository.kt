package com.example.loginmultiplatform.repository

import com.example.loginmultiplatform.model.ResponseWrapper
import com.example.loginmultiplatform.model.StudentAssignmentRequest
import com.example.loginmultiplatform.model.StudentAssignmentResponse
import com.example.loginmultiplatform.model.TeacherAssignmentResponse
import com.example.loginmultiplatform.network.ApiClient
import com.example.loginmultiplatform.network.ApiService

class StudentHomeworkRepository {
    private val apiService = ApiClient.retrofit.create(ApiService::class.java)

    suspend fun fetchStudentAssignments(studentId: Int): ResponseWrapper<List<StudentAssignmentResponse>> {
        val response = apiService.fetchStudentAssignments(studentId)

        if (response.success) {
            return response
        } else {
            throw Exception(response.message)
        }
    }

    suspend fun submitAssignment(
        assignmentId: Int,
        submission: StudentAssignmentRequest
    ): ResponseWrapper<StudentAssignmentResponse> {
        val response = apiService.submitAssignment(assignmentId, submission)

        if (response.success) {
            return response
        } else {
            throw Exception(response.message)
        }
    }

    suspend fun unSubmitAssignment(
        assignmentId: Int
    ): ResponseWrapper<TeacherAssignmentResponse> {
        val response = apiService.unSubmitAssignment(assignmentId)

        if (response.success) {
            return response
        } else {
            throw Exception(response.message)
        }
    }

    suspend fun downloadTeacherDocument(documentId: Int): Result<String> {
        return try {
            val documentContent = apiService.downloadTeacherDocument(documentId)
            Result.success(documentContent)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}