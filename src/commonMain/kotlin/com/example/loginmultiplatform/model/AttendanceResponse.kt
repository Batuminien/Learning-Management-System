package com.example.loginmultiplatform.model

data class ResponseWrapper<T>(
    val success: Boolean,
    val message: String,
    val data: T
)

data class AttendanceResponse(
    val attendanceId: Long,
    val studentId: Long,
    val studentName: String,
    val studentSurname: String,
    val date: String,
    val status: String, //"PRESENT", "ABSENT", or "LATE"
    val comment: String?,
    val classId: Long,
    val courseId: Long
)

data class AttendanceStats(
    val studentId: Int,
    val studentName: String,
    val classId: Int,
    val className: String,
    val courseId: Int,
    val courseName: String,
    val attendancePercentage: Double,
    val totalClasses: Int,
    val presentCount: Int,
    val absentCount: Int,
    val lateCount: Int,
    val recentAttendance: List<AttendanceResponse>?
)
