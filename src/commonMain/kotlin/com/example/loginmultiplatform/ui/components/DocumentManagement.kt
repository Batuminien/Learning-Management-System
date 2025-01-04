package com.example.loginmultiplatform.ui.components

import androidx.compose.runtime.Composable


@Composable
expect fun rememberDocumentManager(onResult: (SharedDocument?) -> Unit): DocumentManager

expect class DocumentManager(
    onLaunch: () -> Unit
) {
    fun launch()
}

expect class SharedDocument {
    fun toByteArray(): ByteArray?
    fun toText(): String?
    fun fileName(): String?
    fun filePath(): String?
    fun fileSize(): Long?
}


expect class PermissionsManager(callback: PermissionCallback) : PermissionHandler

interface PermissionCallback {
    fun onPermissionStatus(permissionType: PermissionType, status: PermissionStatus)
}

@Composable
expect fun createPermissionsManager(callback: PermissionCallback): PermissionsManager

interface PermissionHandler {
    @Composable
    fun askPermission(permission: PermissionType)

    @Composable
    fun isPermissionGranted(permission: PermissionType): Boolean

    @Composable
    fun launchSettings()
}

enum class PermissionType {
    GALLERY,
    DOCUMENT
}

enum class PermissionStatus {
    GRANTED,
    DENIED,
    SHOW_RATIONALE
}