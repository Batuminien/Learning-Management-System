package com.example.loginmultiplatform.model

import kotlinx.datetime.LocalTime

data class CourseSchedule (
    val id : Long,
    val teacherCourseId : Long,
    val teacherName : String,
    val teacherCourseName : String,
    val classId : Long,
    val className : String,
    val dayOfWeek : String, // "MONDAY" "TUESDAY" "WEDNESDAY" "THURSDAY" "FRIDAY" "SATURDAY" "SUNDAY"
    val startTime : String,
    val endTime : String,
    val location : String
)


