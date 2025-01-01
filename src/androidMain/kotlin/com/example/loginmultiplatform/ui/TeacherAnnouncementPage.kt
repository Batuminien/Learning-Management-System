package com.example.loginmultiplatform.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MarkEmailRead
import androidx.compose.material.icons.rounded.MarkEmailUnread
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.loginmultiplatform.model.StudentAnnouncementResponse
import com.example.loginmultiplatform.model.TeacherClassResponse
import com.example.loginmultiplatform.viewmodel.LoginViewModel
import com.example.loginmultiplatform.viewmodel.StudentAnnouncementViewModel
import com.example.loginmultiplatform.viewmodel.TeacherAttendanceViewModel
import com.example.loginmultiplatform.viewmodel.AdministratorAnnouncementsViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@SuppressLint("NewApi")
fun getCurrentIsoDateTime(): String {
    val localDateTime = LocalDateTime.now(ZoneId.of("Europe/Istanbul"))
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS") // Zaman dilimi ofseti olmadan
    return formatter.format(localDateTime)
}

@SuppressLint("NewApi")
fun formatToReadableDateTimeAnn(isoDateTime: String): String {
    val formatters = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
        "yyyy-MM-dd'T'HH:mm:ss.SSSSS",
        "yyyy-MM-dd'T'HH:mm:ss.SSSS",
        "yyyy-MM-dd'T'HH:mm:ss.SSS",
        "yyyy-MM-dd'T'HH:mm:ss.SS",
        "yyyy-MM-dd'T'HH:mm:ss.S",
        "yyyy-MM-dd'T'HH:mm:ss"
    ).map { DateTimeFormatter.ofPattern(it) }

    return try {
        // İlk uygun formatı bul ve parse et
        val localDateTime = formatters.firstNotNullOfOrNull { formatter ->
            try {
                LocalDateTime.parse(isoDateTime, formatter)
            } catch (e: Exception) {
                null
            }
        } ?: throw IllegalArgumentException("Invalid date format")

        // Çıkış formatı
        val outputFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy HH:mm", Locale("tr", "TR"))

        // Formatlanmış string döndür
        outputFormatter.format(localDateTime)
    } catch (e: Exception) {
        e.printStackTrace() // Hata ayıklama için
        "Geçersiz Tarih"
    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "StateFlowValueCalledInComposition")
@Composable
actual fun TeacherAnnouncementPage(loginViewModel: LoginViewModel, teacherAnnouncementViewModel: AdministratorAnnouncementsViewModel, teacherAttendanceViewModel: TeacherAttendanceViewModel, studentAnnouncementViewModel: StudentAnnouncementViewModel, navController: NavController) {
    val classes by teacherAttendanceViewModel.teacherClasses.collectAsState()
    val announcements by studentAnnouncementViewModel.announcement.collectAsState()
    var showAddAnnouncementCard by remember { mutableStateOf(false) }
    val saveOperationStatus by teacherAnnouncementViewModel.saveOperationStatus.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val userRole = loginViewModel.role.value
    val allClasses = teacherAttendanceViewModel.allClass.collectAsState().value
    val classesToFetch = if (userRole == "ROLE_COORDINATOR" || userRole == "ROLE_ADMIN") allClasses else classes

    LaunchedEffect(Unit) {
        teacherAttendanceViewModel.fetchTeacherClasses()
        if (userRole == "ROLE_COORDINATOR" || userRole == "ROLE_ADMIN") {
            teacherAttendanceViewModel.fetchAllClasses()
        }

        classesToFetch.forEach { classInfo ->
            studentAnnouncementViewModel.fetchAnnouncementsByClassId(classInfo.id)
        }
    }

    /*LaunchedEffect(classesToFetch) {

    }*/

    // Snackbar gösterimi
    LaunchedEffect(saveOperationStatus) {
        saveOperationStatus?.let { message ->
            snackbarHostState.showSnackbar(message)
            classesToFetch.forEach { classInfo ->
                studentAnnouncementViewModel.fetchAnnouncementsByClassId(classInfo.id)
            }

            teacherAnnouncementViewModel.clearSaveOperationStatus()
        }
    }

    Scaffold(
        snackbarHost = { CustomSnackbar(snackbarHostState = snackbarHostState) },
        content = {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .clickable { showAddAnnouncementCard = !showAddAnnouncementCard }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Duyuru Ekle",
                                tint = Color(0xFF334BBE)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Duyuru Ekle",
                                fontFamily = customFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF334BBE)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }



                if (announcements.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Henüz duyuru yok.",
                                style = MaterialTheme.typography.body1,
                                fontFamily = customFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    announcements.forEach { announcement ->
                        item {
                            TeacherAnnouncementCard(announcement,
                                announcements,
                                onDeleteAnnouncement = { announcementId ->
                                    Log.e("deleteAnnouncementId", "id: ${announcementId}")
                                    coroutineScope.launch {
                                        teacherAnnouncementViewModel.deleteAnnouncement(
                                            announcementId
                                        )
                                        saveOperationStatus?.let { it1 ->
                                            snackbarHostState.showSnackbar(
                                                it1
                                            )
                                        }
                                        classesToFetch.forEach { classInfo ->
                                            studentAnnouncementViewModel.fetchAnnouncementsByClassId(
                                                classInfo.id
                                            )
                                        }
                                    }
                                },
                                administratorAnnouncementsViewModel = teacherAnnouncementViewModel,
                                onRefreshAnnouncements = {
                                    coroutineScope.launch {
                                        classesToFetch.forEach { classInfo ->
                                            studentAnnouncementViewModel.fetchAnnouncementsByClassId(
                                                classInfo.id
                                            )
                                        }
                                    }
                                },
                                onUpdateAnnouncement = { updatedAnnouncement, attendanceId ->
                                    coroutineScope.launch {
                                        if (attendanceId != null) {
                                            teacherAnnouncementViewModel.updateAnnouncement(attendanceId, updatedAnnouncement)
                                        }
                                        saveOperationStatus?.let { message ->
                                            snackbarHostState.showSnackbar(message)
                                        }

                                        classesToFetch.forEach { classInfo ->
                                            studentAnnouncementViewModel.fetchAnnouncementsByClassId(classInfo.id)
                                        }
                                    }
                                },
                                classesToFetch
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                if (showAddAnnouncementCard) {
                    item {
                        AddAnnouncementCard(
                            classesToShow = classesToFetch,
                            onAddAnnouncement = { announcement ->
                                teacherAnnouncementViewModel.saveAnnouncements(announcement)
                                showAddAnnouncementCard = false

                                coroutineScope.launch {
                                    saveOperationStatus?.let { message ->
                                        snackbarHostState.showSnackbar(message)
                                        classesToFetch.forEach { classInfo ->
                                            studentAnnouncementViewModel.fetchAnnouncementsByClassId(
                                                classInfo.id
                                            )
                                        }
                                    }

                                    val latestAnnouncement =
                                        announcements.maxByOrNull { it.createdAt }
                                    latestAnnouncement?.id?.let { announcementId ->
                                        teacherAnnouncementViewModel.notifyAnnouncement(
                                            announcementId
                                        )
                                    }
                                }
                            },
                            onCancel = {
                                showAddAnnouncementCard = false
                            }
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun AddAnnouncementCard(
    classesToShow: List<TeacherClassResponse>,
    onAddAnnouncement: (StudentAnnouncementResponse) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    val selectedClasses = remember { mutableStateMapOf<Int, Boolean>() }
    var allSelected by remember { mutableStateOf(false) }

    classesToShow.forEach { classInfo ->
        if (!selectedClasses.containsKey(classInfo.id)) {
            selectedClasses[classInfo.id] = false
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Yeni Duyuru",
                fontWeight = FontWeight.Bold,
                fontFamily = customFontFamily,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = {
                    Text(
                        "Başlık",
                        fontFamily = customFontFamily,
                        color = Color(0xFF334BBE),
                        fontSize = 12.sp
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(color = Color.Black, fontSize = 14.sp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    unfocusedBorderColor = Color.Black,
                    cursorColor = Color(0xFF334BBE),
                    focusedBorderColor = Color(0xFF334BBE)
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = {
                    Text(
                        "İçerik",
                        fontFamily = customFontFamily,
                        color = Color(0xFF334BBE),
                        fontSize = 12.sp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 6,
                textStyle = LocalTextStyle.current.copy(color = Color.Black, fontSize = 14.sp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    unfocusedBorderColor = Color.Black,
                    cursorColor = Color(0xFF334BBE),
                    focusedBorderColor = Color(0xFF334BBE)
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("İlgili Sınıflar:", fontWeight = FontWeight.Bold, fontFamily = customFontFamily, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = allSelected,
                        onCheckedChange = { isChecked ->
                            allSelected = isChecked
                            classesToShow.forEach { classInfo ->
                                selectedClasses[classInfo.id] = isChecked
                            }
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF334BBE),
                            uncheckedColor = Color.Gray,
                            checkmarkColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tümü",
                        fontWeight = FontWeight.Bold,
                        fontFamily = customFontFamily
                    )
                }



                classesToShow.forEach { classInfo ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedClasses[classInfo.id] ?: false,
                            onCheckedChange = { isChecked ->
                                selectedClasses[classInfo.id] = isChecked
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF334BBE),
                                uncheckedColor = Color.Gray,
                                checkmarkColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = classInfo.name,
                            fontFamily = customFontFamily,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { onCancel() },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(237,242,254)),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("İptal", color = Color(0xFF334BBE))
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val selectedClassIds = selectedClasses.filter { it.value }.keys.toList()
                            val announcement = StudentAnnouncementResponse(
                                id = null,
                                title = title,
                                content = content,
                                classIds = selectedClassIds,
                                createdAt = getCurrentIsoDateTime(),
                                readAt = "",
                                read = false
                            )
                            onAddAnnouncement(announcement)
                        },
                        enabled = title.isNotBlank() && content.isNotBlank() && selectedClasses.containsValue(true),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFF334BBE),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Ekle", fontFamily = customFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherAnnouncementCard(
    announcement: StudentAnnouncementResponse,
    announcements: List<StudentAnnouncementResponse>,
    onDeleteAnnouncement: (Int) -> Unit,
    administratorAnnouncementsViewModel: AdministratorAnnouncementsViewModel,
    onRefreshAnnouncements: () -> Unit,
    onUpdateAnnouncement: (StudentAnnouncementResponse, Int?) -> Unit,
    classes: List<TeacherClassResponse>
) {
    var isExpanded by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    val backgroundColor = if (announcement.read) Color.White else Color(237,242,254)
    val textColor = if (announcement.read) Color.Black else Color(51,75,190)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (isExpanded) {
                    isEditing = false
                    isExpanded = false
                } else {
                    if(!announcement.read) {
                        announcement.id?.let { announcementId ->
                            administratorAnnouncementsViewModel.readAnnouncement(announcementId)
                        }
                        onRefreshAnnouncements()
                    }
                    isExpanded = true
                }
            }
            .background(backgroundColor),
        elevation = 4.dp,
        backgroundColor = Color.White
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = announcement.title,
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = customFontFamily,
                    color = textColor,
                    modifier = Modifier.weight(1f)
                )

                if (isExpanded) {
                    IconButton(
                        onClick = {
                            val targetAnnouncement = announcements.find {
                                it.title == announcement.title && it.content == announcement.content
                            }
                            targetAnnouncement?.id?.let { id ->
                                onDeleteAnnouncement(id)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Duyuruyu Sil",
                            tint = Color.Red
                        )
                    }

                    IconButton(
                        onClick = { isEditing = true }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Duyuruyu Düzenle",
                            tint = Color(0xFF334BBE)
                        )
                    }
                }

                Icon(
                    imageVector = if (announcement.read) Icons.Rounded.MarkEmailRead else Icons.Rounded.MarkEmailUnread,
                    contentDescription = "Okundu/Okunmadı",
                    tint = textColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = announcement.content,
                fontFamily = customFontFamily,
                fontWeight = FontWeight.Normal,
                maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.body2,
                color = textColor,
                fontSize = 14.sp
            )
            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Yayınlanma Tarihi: ${formatToReadableDateTimeAnn(announcement.createdAt)}",
                    style = MaterialTheme.typography.caption,
                    fontFamily = customFontFamily,
                    color = textColor
                )
            }
            if (announcement.read && isExpanded) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Okunma Tarihi: ${formatToReadableDateTimeAnn(announcement.readAt)}",
                    style = MaterialTheme.typography.caption,
                    color = textColor,
                    fontFamily = customFontFamily
                )
            }

            if (isEditing) {
                EditAnnouncementForm(
                    announcement = announcement,
                    onCancel = { isEditing = false },
                    onSave = { updateAnnouncement, attendanceId ->
                        onUpdateAnnouncement(updateAnnouncement, attendanceId)
                        isEditing = false
                    },
                    classes
                )
            }
        }
    }
}

@Composable
fun EditAnnouncementForm(
    announcement: StudentAnnouncementResponse,
    onCancel: () -> Unit,
    onSave: (StudentAnnouncementResponse, Int?) -> Unit,
    classes: List<TeacherClassResponse>
) {
    var title by remember { mutableStateOf(announcement.title) }
    var content by remember { mutableStateOf(announcement.content) }
    val selectedClasses = remember { mutableStateMapOf<Int, Boolean>() }

    announcement.classIds.forEach { classId ->
        selectedClasses[classId] = true
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {}
            ),
        //elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Duyuruyu Düzenle",
                fontWeight = FontWeight.Bold,
                fontFamily = customFontFamily,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = {
                    Text(
                        "Başlık",
                        fontFamily = customFontFamily,
                        color = Color(0xFF334BBE),
                        fontSize = 12.sp
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(color = Color.Black, fontSize = 14.sp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    unfocusedBorderColor = Color.Black,
                    cursorColor = Color(0xFF334BBE),
                    focusedBorderColor = Color(0xFF334BBE)
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = {
                    Text(
                        "İçerik",
                        fontFamily = customFontFamily,
                        color = Color(0xFF334BBE),
                        fontSize = 12.sp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 6,
                textStyle = LocalTextStyle.current.copy(color = Color.Black, fontSize = 14.sp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    unfocusedBorderColor = Color.Black,
                    cursorColor = Color(0xFF334BBE),
                    focusedBorderColor = Color(0xFF334BBE)
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("İlgili Sınıflar:", fontWeight = FontWeight.Bold, fontFamily = customFontFamily, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))
            Column {
                classes.forEach { classInfo ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedClasses[classInfo.id] == true,
                            onCheckedChange = { checked ->
                                selectedClasses[classInfo.id] = checked
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF334BBE),
                                uncheckedColor = Color.Gray,
                                checkmarkColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = classInfo.name, // Sınıf isimlerini burada değiştirebilirsiniz
                            fontFamily = customFontFamily,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onCancel,
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(237,242,254)),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("İptal", color = Color(0xFF334BBE))
                }
                Button(
                    onClick = {
                        val updatedAnnouncement = announcement.copy(
                            title = title,
                            content = content,
                            classIds = selectedClasses.filter { it.value }.keys.toList(),
                            createdAt = getCurrentIsoDateTime(),
                            readAt = "",
                            read = false
                        )
                        val attendanceId = announcement.id
                        onSave(updatedAnnouncement, attendanceId)
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF334BBE),
                        contentColor = Color.White
                    )
                ) {
                    Text("Güncelle")
                }
            }
        }
    }
}