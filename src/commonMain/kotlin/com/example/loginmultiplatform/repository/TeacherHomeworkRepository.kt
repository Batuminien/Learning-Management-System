package com.example.loginmultiplatform.repository

import com.example.loginmultiplatform.model.AssignmentDocument
import com.example.loginmultiplatform.model.TeacherClassResponse
import com.example.loginmultiplatform.model.TeacherCourseResponse
import com.example.loginmultiplatform.model.TeacherAssignmentResponse
import com.example.loginmultiplatform.model.ResponseWrapper
import com.example.loginmultiplatform.model.TeacherAssignmentRequest
import com.example.loginmultiplatform.network.ApiClient
import com.example.loginmultiplatform.network.ApiService


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

    suspend fun uploadDocument(assignmentId : Long, docContent : String) : ResponseWrapper<AssignmentDocument> {
        val response = apiService.newDocument(assignmentId, docContent)

        if (response.success){
            return response
        }else {
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



}