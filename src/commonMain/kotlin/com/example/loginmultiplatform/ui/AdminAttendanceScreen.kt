package com.example.loginmultiplatform.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.loginmultiplatform.viewmodel.AttendanceViewModel
import com.example.loginmultiplatform.viewmodel.TeacherAttendanceViewModel

@Composable
expect fun AdminAttendanceScreen(studentViewModel: AttendanceViewModel, teacherViewModel: TeacherAttendanceViewModel = TeacherAttendanceViewModel() ,navController: NavController)
