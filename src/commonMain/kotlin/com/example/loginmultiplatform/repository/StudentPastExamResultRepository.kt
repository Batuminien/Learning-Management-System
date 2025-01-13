package com.example.loginmultiplatform.repository

import com.example.loginmultiplatform.model.ResponseWrapper
import com.example.loginmultiplatform.model.StudentDashboard
import com.example.loginmultiplatform.model.StudentExamResultsResponses
import com.example.loginmultiplatform.network.ApiClient
import com.example.loginmultiplatform.network.ApiService

class StudentPastExamResultRepository {
    private val apiService = ApiClient.retrofit.create(ApiService::class.java)


    suspend fun fetchStudentPastExamResults (studentId : Long) : ResponseWrapper<List<StudentExamResultsResponses>> {

        val response = apiService.fetchStudentPastExamResults(studentId = studentId)

        if (response.success){
            return response
        }else{
            throw Exception(response.message)
        }
    }

    suspend fun dashboard (studentId: Long) : ResponseWrapper<List<StudentDashboard>> {

        val response = apiService.dashboard(studentId = studentId)

        if (response.success){
            return response
        }else{
            throw Exception(response.message)
        }

    }

}