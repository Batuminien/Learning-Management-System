package com.example.loginmultiplatform.repository

import com.example.loginmultiplatform.model.CourseSchedule
import com.example.loginmultiplatform.model.ResponseWrapper
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



}