package com.example.loginmultiplatform.repository

import androidx.annotation.Nullable
import com.example.loginmultiplatform.network.ApiClient
import com.example.loginmultiplatform.model.StudentAnnouncementResponse
import com.example.loginmultiplatform.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AdministratorsAnnouncementsRepository {
    private val apiService = ApiClient.retrofit.create(ApiService::class.java)

    suspend fun saveAnnouncements(
        announcement: StudentAnnouncementResponse
    ): StudentAnnouncementResponse {
        val response = apiService.saveAnnouncements(announcement)

        if (response.success) {
            return response.data
        } else {
            throw Exception(response.message)
        }
    }

    suspend fun notifyAnnouncement(
        announcementId: Int
    ): Nullable {
        val response = apiService.notifyAnnouncement(announcementId)

        if (response.success) {
            return response.data
        } else {
            throw Exception(response.message)
        }
    }

    suspend fun deleteAnnouncement(
        announcementId: Int
    ): Nullable {
        val response = apiService.deleteAnnouncement(announcementId)

        if(response.success) {
            return response.data
        } else {
            throw Exception(response.message)
        }
    }

    suspend fun readAnnouncement(
        announcementId: Int
    ): Nullable {
        val response = apiService.readAnnouncement(announcementId)

        if (response.success) {
            return response.data
        } else {
            throw Exception(response.message)
        }
    }

    suspend fun updateAnnouncement(
        announcementId: Int,
        announcement: StudentAnnouncementResponse
    ): StudentAnnouncementResponse {
        val response = apiService.updateAnnouncement(announcementId, announcement)

        if (response.success) {
            return response.data
        } else {
            throw Exception(response.message)
        }
    }
}