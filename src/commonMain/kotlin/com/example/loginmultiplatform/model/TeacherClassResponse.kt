package com.example.loginmultiplatform.model

data class TeacherClassResponse(
    val id: Int,
    val name: String,
    val description: String,
    val classId: Int,
    val teacherId: Int,
    val studentIdAndNames: Map<String, String>,
    val assignmentIds: List<Int>
)