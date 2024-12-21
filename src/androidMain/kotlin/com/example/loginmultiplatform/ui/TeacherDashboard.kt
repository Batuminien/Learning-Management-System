package com.example.loginmultiplatform.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.loginmultiplatform.ui.components.BottomNavigationBar
import com.example.loginmultiplatform.ui.components.TopBar
import com.example.loginmultiplatform.viewmodel.AttendanceViewModel
import com.example.loginmultiplatform.viewmodel.LoginViewModel
import com.example.loginmultiplatform.viewmodel.TeacherAttendanceViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
actual fun TeacherDashboard(loginViewModel: LoginViewModel, studentViewModel: AttendanceViewModel, teacherAttendanceViewModel: TeacherAttendanceViewModel, navController: NavController) {
    val pagerState = rememberPagerState(initialPage = 1, pageCount = { 3 })
    //val studentId by viewModel.studentId.collectAsState()
    val username by loginViewModel.username.collectAsState()

    Scaffold(
        topBar = { TopBar(userName = username, onSettingsClick = { }, onProfileClick = {}) },
        bottomBar = { BottomNavigationBar(pagerState = pagerState) }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { page ->
            when (page) {
                0 -> TeacherAttendanceScreen(studentViewModel, teacherAttendanceViewModel, navController)
                1 -> TeacherDashboardPage(username ?: "-")
                2 -> TeacherHomeworkPage("TEACHER HOMEWORK PAGE")
            }
        }
    }
}

@Composable
fun TeacherDashboardPage(username: String) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = username,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            fontFamily = customFontFamily
        )
    }
}

@Composable
fun TeacherHomeworkPage(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            fontFamily = customFontFamily
        )
    }
}
