package com.example.loginmultiplatform.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.loginmultiplatform.ui.components.BottomNavigationBar
import com.example.loginmultiplatform.ui.components.TopBar
import com.example.loginmultiplatform.viewmodel.LoginViewModel
import com.example.loginmultiplatform.viewmodel.ProfilePhotoViewModel
import com.example.loginmultiplatform.viewmodel.TeacherAttendanceViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.FilePermission
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

fun formatToReadableDatePp(dateString: String): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val date = inputFormat.parse(dateString)

    val outputFormat = SimpleDateFormat("d MMMM yyyy", Locale("tr"))
    return outputFormat.format(date)
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
actual fun ProfilePage(loginViewModel: LoginViewModel, profilePhotoUrl: String?, userId: Int, navController: NavController) {

    val profilePhotoViewModel: ProfilePhotoViewModel = remember { ProfilePhotoViewModel() }
    val teacherAttendanceViewModel = TeacherAttendanceViewModel()
    val studentInfo by profilePhotoViewModel.studentInfo.collectAsState()
    val id by loginViewModel.id.collectAsState()
    val classes by teacherAttendanceViewModel.allClass.collectAsState()

    LaunchedEffect(id) {
        id?.let { profilePhotoViewModel.fetchStudentInfo(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ProfilePhotoSection(
            profilePhotoUrl = profilePhotoUrl,
            profilePhotoViewModel = profilePhotoViewModel
        )

        Spacer(modifier = Modifier.height(16.dp))

        Divider(thickness = 1.dp)
        Spacer(modifier = Modifier.height(16.dp))
        // Kullanıcı Bilgileri
        SectionTitle(title = "Kullanıcı Bilgileri")
        Spacer(modifier = Modifier.height(8.dp))

        UserInfoRow(label = "Kullanıcı Adı:", value = studentInfo?.username ?: "")
        UserInfoRow(label = "Email:", value = studentInfo?.email ?: "")

        Spacer(modifier = Modifier.height(16.dp))
        Divider(thickness = 1.dp)
        Spacer(modifier = Modifier.height(16.dp))
        SectionTitle(title = "Özlük Bilgileri")
        Spacer(modifier = Modifier.height(8.dp))

        UserInfoRow(
            label = "Ad Soyad:",
            value = (studentInfo?.firstName ?: "") + " " + (studentInfo?.lastName ?: "")
        )
        studentInfo?.birthDate?.let { UserInfoRow(label = "Doğum Tarihi:", value = formatToReadableDatePp(it)) }
        studentInfo?.registrationDate?.let { UserInfoRow(label = "Kayıt Tarihi:", value = formatToReadableDatePp(it)) }
        studentInfo?.phone?.let { UserInfoRow(label = "Telefon No:", value = it) }
        studentInfo?.tc?.let { UserInfoRow(label = "T.C. No:", value = it) }

        // Sınıf eşleşmesi
        val matchedClass = classes.firstOrNull { it.id == studentInfo?.classId }
        matchedClass?.let {
            UserInfoRow(label = "Sınıfı:", value = it.name)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Divider(thickness = 1.dp)
        Spacer(modifier = Modifier.height(16.dp))
        SectionTitle(title = "Veli Bilgileri")
        Spacer(modifier = Modifier.height(8.dp))

        studentInfo?.parentName?.let { UserInfoRow(label = "Veli Adı:", value = it) }
        studentInfo?.parentPhone?.let { UserInfoRow(label = "Veli Telefon No:", value = it) }

        Spacer(modifier = Modifier.height(16.dp))
        Divider(thickness = 1.dp)
    }
}

@Composable
fun ProfilePhotoSection(
    profilePhotoUrl: String?,
    profilePhotoViewModel: ProfilePhotoViewModel
) {
    Box(
        contentAlignment = Alignment.BottomEnd,
        modifier = Modifier.size(101.dp)
    ) {
        Box(
            modifier = Modifier
                .size(101.dp)
                .clip(CircleShape)
                .background(Color(0xFF334BBE)),
            contentAlignment = Alignment.Center
        ) {
            if (profilePhotoUrl == "default_url") {
                Icon(
                    imageVector = Icons.Rounded.AccountCircle,
                    contentDescription = "Default Profile Picture",
                    tint = Color.White,
                    modifier = Modifier
                        .size(110.dp)
                )
            } else {
                AsyncImage(
                    model = profilePhotoUrl,
                    contentDescription = "Profile Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        UploadProfilePhoto(profilePhotoViewModel)
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF334BBE),
        fontFamily = customFontFamily
    )
}

@Composable
fun UserInfoRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = customFontFamily,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = customFontFamily,
            color = Color.Black
        )
    }
}

@Composable
fun UploadProfilePhoto(
    profilePhotoViewModel: ProfilePhotoViewModel
) {
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                uploadPhoto(context, uri, profilePhotoViewModel)
            }
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = { bitmap ->
            bitmap?.let {
                val uri = saveBitmapToUri(context, it)
                uri?.let {
                    uploadPhoto(context, it, profilePhotoViewModel)
                }
            }
        }
    )

    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Profil Fotoğrafı Yükle") },
            text = {
                Column {
                    TextButton(onClick = {
                        showDialog = false
                        galleryLauncher.launch("image/*")
                    }) {
                        Text("Galeriden Seç")
                    }
                    TextButton(onClick = {
                        showDialog = false
                        cameraLauncher.launch(null)
                    }) {
                        Text("Kameradan Çek")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }

    IconButton(
        onClick = { showDialog = true },
        modifier = Modifier
            .size(24.dp)
            .offset(x = (-8).dp, y = (-8).dp)
            .clip(CircleShape)
            .background(Color(0xFF334BBE))
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = "Change Profile Photo",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

fun uploadPhoto(context: Context, uri: Uri, profilePhotoViewModel: ProfilePhotoViewModel) {
    val pngUri = convertUriToPngUri(context, uri)
    pngUri?.let {
        val contentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(it)
        val requestBody = inputStream?.readBytes()?.let { bytes ->
            RequestBody.create("image/*".toMediaTypeOrNull(), bytes)
        }
        val filePart =
            requestBody?.let { MultipartBody.Part.createFormData("file", "profile_photo.jpeg", it) }

        if (filePart != null) {
            profilePhotoViewModel.uploadPp(filePart)
        }
    }
}

fun saveBitmapToUri(context: Context, bitmap: Bitmap): Uri? {
    return try {
        val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.png")
        val outputStream = FileOutputStream(file)

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.flush()
        outputStream.close()

        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

fun convertUriToPngUri(context: Context, uri: Uri): Uri? {
    return try {
        val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        saveBitmapToUri(context, bitmap)
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}