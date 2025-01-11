package com.example.loginmultiplatform.repository

import android.content.Context
import com.example.loginmultiplatform.model.AssignmentDocument
import com.example.loginmultiplatform.model.BulkGradeRequest
import com.example.loginmultiplatform.model.TeacherClassResponse
import com.example.loginmultiplatform.model.TeacherCourseResponse
import com.example.loginmultiplatform.model.TeacherAssignmentResponse
import com.example.loginmultiplatform.model.ResponseWrapper
import com.example.loginmultiplatform.model.StudentClassResponse
import com.example.loginmultiplatform.model.TeacherAssignmentRequest
import com.example.loginmultiplatform.network.ApiClient
import com.example.loginmultiplatform.network.ApiService
import com.example.loginmultiplatform.ui.components.SharedDocument
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File


class TeacherHomeworkRepository {
    private val apiService = ApiClient.retrofit.create(ApiService::class.java)



    suspend fun updateHomework(assignmentId: Long, newAssignment: TeacherAssignmentRequest) : ResponseWrapper<TeacherAssignmentResponse> {
        val response = apiService.updateHomework(assignmentId, newAssignment)

        if (response.success){
            return response
        }else {
            throw Exception(response.message)
        }
    }

    suspend fun deleteAssignment(assignmentId: Long){
        apiService.deleteAssignment(assignmentId = assignmentId)
    }

    suspend fun deleteDocument (documentId : Long) {
        apiService.deleteDocument(documentId)
    }


    suspend fun uploadDocument(context: Context, sharedDocument: SharedDocument, assignmentId: Long): AssignmentDocument? {
        val file = sharedDocument.toFile(context) ?: run {
            println("Failed to resolve file from URI.")
            return null
        }
        println("Uploading file: ${file.name}, size: ${file.length()}, path: ${file.absolutePath}")

        // Ensure the file exists
        if (!file.exists()) {
            println("File does not exist: ${file.absolutePath}")
            return null
        }

        // Create RequestBody for the file
        val requestBody = file.asRequestBody("application/octet-stream".toMediaTypeOrNull())

        // Create MultipartBody.Part
        val multipartFile = MultipartBody.Part.createFormData("file", file.name, requestBody)

        // Make the API call
        val response = apiService.uploadDocument(assignmentId, multipartFile)

        if (response.success){
            return response.data
        }else{
            throw Exception(response.message)
        }
    }


    suspend fun fetchTeacherAssignments(teacherId : Int, classId : Int, courseId : Int, dueDate: String) : ResponseWrapper<List<TeacherAssignmentResponse>>{

        val response = apiService.fetchTeacherAssignments(teacherId, classId, courseId, dueDate)

        if (response.success){
            return response
        }else{
            throw Exception(response.message)
        }


    }

    suspend fun getTeacherClasses(): ResponseWrapper<List<TeacherClassResponse>> {
        val response = apiService.fetchTeacherClasses()

        if (response.success) {
            return response
        } else {
            throw Exception(response.message)
        }
    }

    suspend fun getTeacherClassCourses(teacherId: Int): ResponseWrapper<List<TeacherCourseResponse>> {
        val courses = apiService.fetchTeacherCourses(teacherId)




        if (courses.success) {
            return courses
        } else {
            throw Exception(courses.message)
        }

    }

    suspend fun TeacherNewAssignment(newAssignment : TeacherAssignmentRequest) : ResponseWrapper<TeacherAssignmentResponse> {
        val response = apiService.newAssignment(newAssignment)

        if (response.success){
            return response
        }else {
            throw Exception(response.message)
        }
    }

    suspend fun getClass(
        classId: Int
    ): StudentClassResponse {
        val response = apiService.getClass(classId)

        if(response.success) {
            return response.data
        } else {
            throw Exception(response.message)
        }
    }

    suspend fun bulkGrades (assignmentId: Long, bulkGrades: BulkGradeRequest) : TeacherAssignmentResponse{
        val response = apiService.bulkGradeSubmissions(assignmentId, bulkGrades)

        if(response.success) {
            return response.data
        } else {
            throw Exception(response.message)
        }
    }



}