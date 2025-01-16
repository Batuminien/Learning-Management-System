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

data class TeacherInfoResponse(
    val id: Int,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: String,
    val phone: String,
    val tc: String,
    val birthDate: String,
    val teacherCourses: List<TeacherInfoCC>
)

data class CoordinatorInfoResponse(
    val id: Int,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: String,
    val schoolLevel: String,
    val phone: String,
    val tc: String,
    val birthDate: String,
    val coordinatorCourses: List<TeacherInfoCC>
)

data class TeacherInfoCC(
    val teacherId: Int,
    val courseId: Int,
    val courseName: String,
    val classIdsAndNames: Map<String, String>
)