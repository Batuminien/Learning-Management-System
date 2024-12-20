package com.example.loginmultiplatform.model

data class CourseStatisticsResponse(
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
    val recentAttendance: List<AttendanceRecord>
)

data class AttendanceRecord(
    val attendanceId: Int,
    val studentId: Int,
    val studentName: String,
    val studentSurname: String,
    val date: String,
    val status: String,
    val comment: String?,
    val classId: Int,
    val courseId: Int
)