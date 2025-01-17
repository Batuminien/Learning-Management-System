package com.example.loginmultiplatform.model

// ResponseWrapper is in AttendanceResponse

data class StudentAssignmentResponse (
    val id: Long,
    val title : String,
    val description : String?,
    val dueDate : String,  // Date format
    val teacherDocument : AssignmentDocument?,
    val mySubmission : StudentSubmission?,
    val createdDate : String, // date format,
    val assignedByTeacherName : String,
    val className : String,
    val courseName : String,
)

// AssignmentDocument & StudentSubmission are in TeacherAssignmentResponse