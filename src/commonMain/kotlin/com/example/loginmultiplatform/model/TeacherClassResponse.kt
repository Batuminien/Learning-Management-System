package com.example.loginmultiplatform.model

data class TeacherClassResponse(
    val id: Int,
    val name: String,
    val description: String,
    val teacherCourses: List<TeacherCourse>,
    val studentIdAndNames: Map<String, String>,
    val assignmentIds: List<Int>
)

data class TeacherCourse(
    val teacherId: Int,
    val courseId: Int,
    val classIds: List<Int>
)
