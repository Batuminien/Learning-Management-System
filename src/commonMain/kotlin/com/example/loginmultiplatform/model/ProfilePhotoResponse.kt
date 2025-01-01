package com.example.loginmultiplatform.model

data class ProfilePhotoResponse (
    val photoUrl: String,
    val filename: String,
    val fileType: String,
    val fileSize: Int,
    val uploadTime: String
)