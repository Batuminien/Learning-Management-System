package com.example.loginmultiplatform.model

data class StudentClassResponse(
    val id: Int,
    val name: String,
    val description: String,
    val teacherCourses: List<TeacherCourse>,
    val studentIdAndNames: Map<String, String>,
    val assignmentIds: List<Int>
)