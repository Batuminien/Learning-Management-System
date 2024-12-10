package com.example.loginmultiplatform.network

import com.example.loginmultiplatform.model.AttendanceResponse
import com.example.loginmultiplatform.model.AttendanceStatsResponse
import com.example.loginmultiplatform.model.ResponseWrapper
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

    /*@GET("/api/v1/attendance/stats/student/{studentId}")
    suspend fun getAttendanceStats(
        @Path("studentId") studentId: Int,
        @Query("classId") classId: Int
    ): ResponseWrapper<AttendanceStatsResponse>*/

    /*@GET("/api/v1/classed/student")
    suspend fun getClassName(
        @Path("classId") classId: Int
    )*/
}

