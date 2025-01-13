package com.example.loginmultiplatform.model

data class StudentAnnouncementResponse(
    val id: Int?,
    val title: String,
    val content: String,
    val classIds: List<Int>,
    val createdAt: String,
    val readAt: String,
    val read: Boolean,
    val createdById: Int,
    val createdByName: String,
    val creatorRole: String,
)