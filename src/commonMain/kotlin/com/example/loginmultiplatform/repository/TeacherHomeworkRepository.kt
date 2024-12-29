package com.example.loginmultiplatform.repository

import com.example.loginmultiplatform.model.TeacherClassResponse
import com.example.loginmultiplatform.model.TeacherCourseResponse
import com.example.loginmultiplatform.model.Assignment
import com.example.loginmultiplatform.model.ResponseWrapper
import com.example.loginmultiplatform.model.TeacherAssignmentRequest
import com.example.loginmultiplatform.model.TeacherAssignmentResponse
import com.example.loginmultiplatform.network.ApiClient
import com.example.loginmultiplatform.network.ApiService


class TeacherHomeworkRepository {
    private val apiService = ApiClient.retrofit.create(ApiService::class.java)

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

    suspend fun TeacherNewAssignment(newAssignment : TeacherAssignmentRequest) : ResponseWrapper<Int> {
        val response = apiService.newAssignment(newAssignment)

        if (response.success){
            return response
        }else {
            throw Exception(response.message)
        }
    }

    suspend fun getTeacherAssignments (teacherId : Int) : ResponseWrapper<List<TeacherAssignmentResponse>> {
        val response = apiService.fetcTeacherHomeworks(teacherId)

        if (response.success) {
            return response
        } else {
            throw Exception(response.message)
        }
    }


}