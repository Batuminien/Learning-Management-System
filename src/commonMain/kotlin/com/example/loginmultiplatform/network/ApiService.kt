package com.example.loginmultiplatform.network

import androidx.annotation.Nullable
import com.example.loginmultiplatform.model.TeacherCourseResponse
import com.example.loginmultiplatform.model.AttendanceResponse
import com.example.loginmultiplatform.model.AttendanceStats
import com.example.loginmultiplatform.model.CourseStatisticsResponse
import com.example.loginmultiplatform.model.LoginResponse
import com.example.loginmultiplatform.model.ResponseWrapper
import com.example.loginmultiplatform.model.StudentAnnouncementResponse
import com.example.loginmultiplatform.model.StudentClassResponse
import com.example.loginmultiplatform.model.StudentCourseResponse
import com.example.loginmultiplatform.model.TeacherAssignmentRequest
import com.example.loginmultiplatform.model.TeacherAttendanceRequest
import com.example.loginmultiplatform.model.TeacherClassResponse
import com.example.loginmultiplatform.model.TeacherAssignmentResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("/api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("/api/v1/attendance/student/{studentId}")
    suspend fun getAttendance(
        @Path("studentId") studentId: Int,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): ResponseWrapper<List<AttendanceResponse>>

    @GET("/api/v1/courses/student/{studentId}")
    suspend fun getStudentCourses(
        @Path("studentId") studentId: Int
    ): ResponseWrapper<List<StudentCourseResponse>>

    @GET("/api/v1/classes/student/{studentId}")
    suspend fun getStudentClass(
        @Path("studentId") studentId: Int
    ): ResponseWrapper<StudentClassResponse>

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

    @GET("/api/v1/courses")
    suspend fun fetchAllCourses() : ResponseWrapper<List<StudentCourseResponse>>

    @GET("/api/v1/classes")
    suspend fun fetchAllClasses() : ResponseWrapper<List<TeacherClassResponse>>

    @PUT("/api/v1/attendance/{attendanceId}")
    suspend fun updateAttendance(
        @Path("attendanceId") attendanceId: Int,
        @Body attendanceList: TeacherAttendanceRequest
    ): ResponseWrapper<AttendanceResponse>

    @GET("/api/v1/announcements/class/{classId}")
    suspend fun fetchAnnouncementsByClassId(
        @Path("classId") classId: Int
    ): ResponseWrapper<List<StudentAnnouncementResponse>>

    @POST("/api/v1/announcements")
    suspend fun saveAnnouncements(
        @Body announcementsBody: StudentAnnouncementResponse
    ): ResponseWrapper<StudentAnnouncementResponse>

    @POST("/api/v1/announcements/{announcementId}/notify")
    suspend fun notifyAnnouncement(
        @Path("announcementId") announcementId: Int
    ): ResponseWrapper<Nullable>

    @POST("/api/v1/announcements/{announcementId}/read")
    suspend fun readAnnouncement(
        @Path("announcementId") announcementId: Int
    ): ResponseWrapper<Nullable>

    @DELETE("/api/v1/announcements/{assignmentId}")
    suspend fun deleteAnnouncement(
        @Path("assignmentId") assignmentId: Int
    ): ResponseWrapper<Nullable>

    @PUT("/api/v1/announcements/{id}")
    suspend fun updateAnnouncement(
        @Path("id") id: Int,
        @Body announcementBody: StudentAnnouncementResponse
    ): ResponseWrapper<StudentAnnouncementResponse>



    @GET("/api/v1/courses/class/{classId}")
    suspend fun fetchClassCourses (
        @Path("classId") classId: Int,
    ): ResponseWrapper<List<TeacherCourseResponse>>



    @POST("/api/v1/assignments/createAssignment")
    suspend fun newAssignment (
        @Body newAssignment : TeacherAssignmentRequest
    ): ResponseWrapper<Int>


    @GET("/api/v1/assignments/teacher/{teacherId}")
    suspend fun fetchTeacherAssignments (
        @Path("teacherId") teacherId: Int,
        @Query("classId") classId: Int,
        @Query("courseId") courseId: Int,
        @Query("dueDate") dueDate: String
    ): ResponseWrapper<List<TeacherAssignmentResponse>>


}

