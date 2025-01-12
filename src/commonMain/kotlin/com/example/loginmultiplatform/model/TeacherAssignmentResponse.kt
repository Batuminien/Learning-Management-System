package com.example.loginmultiplatform.model

import androidx.compose.runtime.MutableState
import kotlinx.coroutines.flow.MutableStateFlow


data class TeacherAssignmentResponse (
    val id: Long,
    val title : String,
    val description : String?,
    val dueDate : String,  // Date format
    val message: String,
    val teacherDocuments : AssignmentDocument?,
    val studentSubmissions : List<StudentSubmission>,
    val createdDate : String, // date format,
    val assignedByTeacherName : String,
    val classId : Int,
    val className : String,
    val courseId : Int,
    val courseName : String,
    val lastModified : String , // Date format
    val lastModifiedByUsername : String


)


data class AssignmentDocument (
    val assignmentId : Long,
    val documentId : Long,
    val fileName : String, // MaxLength: 255, MinLength: 0
    val fileType : String, // maxLength: 50, MinLength : 0
    val filePath : String,
    val fileSize : Long,
    val uploadTime : String, // Date and time format
    val uploadedByUsername : String
)


data class StudentSubmission (
    val id : Long,
    val studentId : Int,
    val studentName : String,
    val status : String, // Three different situation will be asked PENDING SUBMITTED GRADED
    val document : AssignmentDocument?,
    val submissionDate : String , // date and time format
    val comment : String?,
    val grade : Long,
    val feedback : String?
)

data class BulkGradeRequest (
    val grades: List<BulkGradeItem>
)

data class BulkGradeItem (
    val studentId : Int,
    val grade : GradeDTO
)


data class GradeDTO (
    val grade : Long,
    val feedback : String?
)