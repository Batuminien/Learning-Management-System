package com.example.loginmultiplatform.model

data class StudentCourseResponse(
    val id: Int,
    val name: String,
    val description: String,
    val code: String,
    val credits: Int,
    val classEntityIds: List<Int>,
    val teacherCourses: List<TeacherCourseClass>
)

data class TeacherCourseClass(
    val teacherId: Int,
    val teacherName: String,
    val courseId: Int,
    val classIds: List<Int>
)