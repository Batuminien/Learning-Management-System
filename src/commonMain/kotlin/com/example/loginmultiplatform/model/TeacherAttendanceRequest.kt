package com.example.loginmultiplatform.model

data class TeacherAttendanceRequest(
    val studentId: Int,
    val date: String,
    val status: String,
    val comment: String,
    val classId: Int,
    val courseId: Int
)