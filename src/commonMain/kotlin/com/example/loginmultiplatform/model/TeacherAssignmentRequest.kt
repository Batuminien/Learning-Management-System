package com.example.loginmultiplatform.model

data class TeacherAssignmentRequest (
    val teacherId : Int,
    val title : String,
    val description : String?,
    val dueDate : String,  // In date format
    val classId : Int,
    val courseId : Int,
    val document : AssignmentDocument?


)