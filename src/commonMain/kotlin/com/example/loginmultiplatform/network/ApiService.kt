package com.example.loginmultiplatform.network

import com.example.loginmultiplatform.model.TeacherCourseResponse
import com.example.loginmultiplatform.model.AttendanceResponse
import com.example.loginmultiplatform.model.AttendanceStats
import com.example.loginmultiplatform.model.CourseStatisticsResponse
import com.example.loginmultiplatform.model.LoginResponse
import com.example.loginmultiplatform.model.ResponseWrapper
import com.example.loginmultiplatform.model.StudentCourseResponse
import com.example.loginmultiplatform.model.TeacherAttendanceRequest
import com.example.loginmultiplatform.model.TeacherClassResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("/api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("/api/v1/attendance/{studentId}")
    suspend fun getAttendance(
        @Path("studentId") studentId: Int,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): ResponseWrapper<List<AttendanceResponse>>

    @GET("/api/v1/courses/student/{studentId}")
    suspend fun getStudentCourses(
        @Path("studentId") studentId: Int
    ): ResponseWrapper<List<StudentCourseResponse>>

    @GET("/api/v1/attendance/stats/student/{studentId}")
    suspend fun getAttendanceStats(
        @Path("studentId") studentId: Int,
        @Query("classId") classId: Int
    ): ResponseWrapper<List<AttendanceStats>>

    @GET("/api/v1/classes/teacher")
    suspend fun fetchTeacherClasses(): ResponseWrapper<List<TeacherClassResponse>>

    @GET("/api/v1/courses/teacher/{teacherId}")
    suspend fun fetchTeacherCourses (
        @Path("teacherId") teacherId: Int,
    ): ResponseWrapper<List<TeacherCourseResponse>>

    @GET("/api/v1/attendance/stats/course/{courseId}")
    suspend fun fetchCourseStatistics(
        @Path("courseId") courseId: Int,
        @Query("classId") classId: Int,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): ResponseWrapper<List<CourseStatisticsResponse>>

    @POST("/api/v1/attendance/bulk")
    suspend fun saveAttendanceBulk(
        @Body attendanceList: List<TeacherAttendanceRequest>
    ): ResponseWrapper<Int>
}

