package com.example.loginmultiplatform.repository

import com.example.loginmultiplatform.model.CourseSchedule
import com.example.loginmultiplatform.model.ResponseWrapper
import com.example.loginmultiplatform.model.TeacherClassResponse
import com.example.loginmultiplatform.model.TeacherCourseResponse
import com.example.loginmultiplatform.model.TeacherInfoResponse
import com.example.loginmultiplatform.network.ApiClient
import com.example.loginmultiplatform.network.ApiService

class CourseScheduleRepository {

    private val apiService = ApiClient.retrofit.create(ApiService::class.java)

    suspend fun getStudentSchedule( studentId: Long ): ResponseWrapper<List<CourseSchedule>> {
        val response = apiService.getStudentSchedule(studentId)

        if (response.success) {
            return response
        } else {
            throw Exception(response.message)
        }
    }


    suspend fun getTeacherSchedule( teacherId: Long ): ResponseWrapper<List<CourseSchedule>> {
        val response = apiService.getTeacherSchedule(teacherId)

        if (response.success) {
            return response
        } else {
            throw Exception(response.message)
        }
    }

    suspend fun getTeacherCourses( teacherId: Int ): ResponseWrapper<List<TeacherCourseResponse>> {
        val response = apiService.getCoursesByTeacher(teacherId)

        if (response.success) {
            return response
        } else {
            throw Exception(response.message)
        }
    }

    suspend fun getTeacherCourseClasses(courseId : Int ,  teacherId: Int ): ResponseWrapper<List<TeacherClassResponse>> {
        val response = apiService.getTeacherCourseClass(courseId, teacherId)

        if (response.success) {
            return response
        } else {
            throw Exception(response.message)
        }
    }



    suspend fun getTeachers() : ResponseWrapper<List<TeacherInfoResponse>>{
        val response = apiService.getAllTeachers()

        if (response.success) {
            return response
        } else {
            throw Exception(response.message)
        }
    }
}