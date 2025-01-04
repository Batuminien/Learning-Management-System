package com.example.loginmultiplatform.ui.components

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

import com.google.accompanist.permissions.ExperimentalPermissionsApi


@Composable
actual fun rememberDocumentManager(onResult: (SharedDocument?) -> Unit): DocumentManager {
    val context = LocalContext.current
    val documentLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                onResult.invoke(SharedDocument(contentResolver = context.contentResolver, uri = it))
            }
        }
    return remember {
        DocumentManager(onLaunch = {
            documentLauncher.launch(arrayOf("application/pdf"))
        })
    }
}

actual class DocumentManager actual constructor(private val onLaunch: () -> Unit) {
    actual fun launch() {
        onLaunch()
    }
}

actual class SharedDocument(private val contentResolver: ContentResolver, private val uri: Uri) {
    actual fun toByteArray(): ByteArray? {
        return contentResolver.openInputStream(uri)?.readBytes()
    }

    actual fun toText(): String? {
        return toByteArray()?.joinToString (separator = "") {
            it.toUByte().toString(2).padStart(8, '0')
        }
    }

    actual fun fileName(): String? {
        var fileName: String? = null
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = it.getString(nameIndex)
                }
            }
        }
        return fileName
    }

    actual fun filePath(): String? {
        return uri.path
    }

    actual fun fileSize() : Long? {
        var size: Long? = null
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex != -1) {
                    size = it.getLong(sizeIndex)
                }
            }
        }
        return size
    }
}


@Composable
actual fun createPermissionsManager(callback: PermissionCallback): PermissionsManager {
    return remember { PermissionsManager(callback) }
}

actual class PermissionsManager actual constructor(private val callback: PermissionCallback) :
    PermissionHandler {
    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    override fun askPermission(permission: PermissionType) {
        callback.onPermissionStatus(permission, PermissionStatus.GRANTED)
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    override fun isPermissionGranted(permission: PermissionType): Boolean {
        return true
    }

    @Composable
    override fun launchSettings() {
        val context = LocalContext.current
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
        context.startActivity(intent)
    }
}



