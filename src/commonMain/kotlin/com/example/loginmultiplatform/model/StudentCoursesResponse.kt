package com.example.loginmultiplatform.model

data class StudentCourseResponse(
    val id: Int,
    val teacherId: Int,
    val name: String,
    val description: String,
    val code: String,
    val credits: Int,
    val classEntityIds: List<Int>
)