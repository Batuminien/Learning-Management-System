package com.example.loginmultiplatform.model

import javax.security.auth.Subject

data class StudentExamResultsResponses (
    val id : Long,
    val studentId : Long,
    val studentName : String,
    val pastExam : PastExamBasic,
    val subjectResults : List<SubjectResultResponse>
)




data class PastExamBasic (
    val id : Long,
    val name : String,
    val examType : String,  // "TYT" "AYT" "YDT" "LGS"
    val overallAverage : Float,
    val date : String // date-time format
)


data class SubjectResultResponse (
    val id : Long,
    val subjectName : String,
    val correctAnswers : Int,
    val incorrectAnswers : Int,
    val blankAnswers : Int,
    val netScore : Float
)


data class StudentDashboard (
    val id : Long,
    val title : String,
    val description: String?,
    val dueDate: String,
    val teacherDocuments: AssignmentDocument?,
    val mySubmission: StudentSubmission?,
    val createdDate: String,
    val assignedByTeacherName: String,
    val className: String,
    val courseName: String

)