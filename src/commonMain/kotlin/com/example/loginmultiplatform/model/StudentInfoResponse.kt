package com.example.loginmultiplatform.model

data class StudentInfoResponse(
    val id: Int,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: String,
    val phone: String,
    val tc: String,
    val birthDate: String,
    val registrationDate: String,
    val parentName: String,
    val parentPhone: String,
    val classId: Int
)