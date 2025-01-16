package com.example.loginmultiplatform.network

data class LoginRequest(
    val username: String,
    val password: String,
    val rememberMe: Boolean
)