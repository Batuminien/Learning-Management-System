package com.example.loginmultiplatform.repository

import com.example.loginmultiplatform.network.ApiClient
import com.example.loginmultiplatform.network.ApiService
import com.example.loginmultiplatform.network.LoginRequest
import com.example.loginmultiplatform.model.LoginData

class UserRepository {
    private val apiService = ApiClient.retrofit.create(ApiService::class.java)

    //suspend fun login(email: String, password: String) = apiService.login(LoginRequest(email, password))

    suspend fun login(username: String, password: String, rememberMe: Boolean): LoginData {
        val response = apiService.login(LoginRequest(username, password, rememberMe))
        if (response.success) {
            return response.data
        } else {
            throw Exception(response.message ?: "Login failed")
        }
    }

    //suspend fun getUser(userId: String) = apiService.getUser(userId)
}