package com.example.loginmultiplatform.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.loginmultiplatform.viewmodel.AttendanceViewModel
import com.example.loginmultiplatform.viewmodel.LoginViewModel
import com.example.loginmultiplatform.viewmodel.TeacherAssignmentViewModel
import com.example.loginmultiplatform.viewmodel.TeacherAttendanceViewModel

@Composable
expect fun TeacherDashboard(loginViewModel: LoginViewModel = LoginViewModel(), studentViewModel: AttendanceViewModel = AttendanceViewModel(), teacherAttendanceViewModel: TeacherAttendanceViewModel = TeacherAttendanceViewModel(), teacherHomeworkViewModel : TeacherAssignmentViewModel = TeacherAssignmentViewModel(), navController: NavController)