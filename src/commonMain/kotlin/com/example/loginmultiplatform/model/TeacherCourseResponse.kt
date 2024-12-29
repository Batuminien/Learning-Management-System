package com.example.loginmultiplatform.model

data class TeacherCourse(
    val TeacherId : Int,
    val courseId : Int,
    val classId : List<Int>
)

data class TeacherCourseClass (
    val courseId : Int,
    val classIds : List<Int>
)


data class TeacherCourseResponse (
    val id : Int,
    val name : String?,
    val description : String,
    val code : String,
    val credits : Int,  // minimum 1
    val classEntityIds : List<Int>,
    val teacherCourses : List<TeacherCourseClass>

)