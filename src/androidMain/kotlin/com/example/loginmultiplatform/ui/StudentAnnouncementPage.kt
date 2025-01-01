package com.example.loginmultiplatform.ui

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.MarkEmailRead
import androidx.compose.material.icons.rounded.MarkEmailUnread
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.loginmultiplatform.viewmodel.AdministratorAnnouncementsViewModel
import com.example.loginmultiplatform.viewmodel.LoginViewModel
import com.example.loginmultiplatform.viewmodel.StudentAnnouncementViewModel

@Composable
actual fun StudentAnnouncementPage(loginViewModel: LoginViewModel, studentAnnouncementViewModel: StudentAnnouncementViewModel, navController: NavController) {

    val announcements by studentAnnouncementViewModel.announcement.collectAsState()
    val studentId by loginViewModel.studentId.collectAsState()
    val classId by studentAnnouncementViewModel.classId.collectAsState()

    val administratorAnnouncementsViewModel = AdministratorAnnouncementsViewModel()

    LaunchedEffect(Unit) {
        studentId?.let { studentAnnouncementViewModel.getStudentClass(it) }
    }

    LaunchedEffect(classId) {
        classId?.let { studentAnnouncementViewModel.fetchAnnouncementsByClassId(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {

        if (announcements.isNotEmpty()) {
            AnnouncementsList(announcements, administratorAnnouncementsViewModel, studentAnnouncementViewModel, classId)
        } else {
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
    }
}

@Composable
fun AnnouncementsList(
    announcements: List<StudentAnnouncementResponse>,
    administratorAnnouncementsViewModel: AdministratorAnnouncementsViewModel,
    studentAnnouncementViewModel: StudentAnnouncementViewModel,
    classId: Int?
) {
    Column {
        announcements.forEach { announcement ->
            AnnouncementCard(
                announcement,
                administratorAnnouncementsViewModel,
                studentAnnouncementViewModel,
                classId
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun AnnouncementCard(
    announcement: StudentAnnouncementResponse,
    administratorAnnouncementsViewModel: AdministratorAnnouncementsViewModel,
    studentAnnouncementViewModel: StudentAnnouncementViewModel,
    classId: Int?
) {
    var isExpanded by remember { mutableStateOf(false) }
    val backgroundColor = if (announcement.read) Color.White else Color(237,242,254)
    val textColor = if (announcement.read) Color.Black else Color(51,75,190)

    Card(
        modifier = Modifier
        .fillMaxWidth()
        .clickable {
            isExpanded = !isExpanded
            if(!announcement.read) {
                announcement.id?.let { announcementId ->
                    administratorAnnouncementsViewModel.readAnnouncement(announcementId)
                    classId?.let { studentAnnouncementViewModel.fetchAnnouncementsByClassId(it) }
                }
            }
        }
            .background(backgroundColor),
        shape = RoundedCornerShape(8.dp),
        elevation = 4.dp
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
                    color = textColor
                )

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
                maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.body2,
                color = textColor,
                fontSize = 14.sp,
                fontFamily = customFontFamily,
                fontWeight = FontWeight.Normal
            )
            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Yayınlanma Tarihi: ${formatToReadableDateTimeAnn(announcement.createdAt)}",
                    style = MaterialTheme.typography.caption,
                    color = textColor,
                    fontFamily = customFontFamily,
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
        }
    }
}