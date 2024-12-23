package com.example.loginmultiplatform.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.loginmultiplatform.viewmodel.AttendanceViewModel
import com.example.loginmultiplatform.viewmodel.TeacherAttendanceViewModel

@Composable
expect fun CoordinatorAttendanceScreen(studentViewModel: AttendanceViewModel, teacherViewModel: TeacherAttendanceViewModel = TeacherAttendanceViewModel() ,navController: NavController)
