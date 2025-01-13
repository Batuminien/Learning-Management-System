package com.example.loginmultiplatform.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.loginmultiplatform.viewmodel.AttendanceViewModel
import com.example.loginmultiplatform.viewmodel.LoginViewModel

@Composable
expect fun StudentDashboard(navController: NavController, loginViewModel: LoginViewModel, attendanceViewModel: AttendanceViewModel, studentId : Int?, classId : Int?)