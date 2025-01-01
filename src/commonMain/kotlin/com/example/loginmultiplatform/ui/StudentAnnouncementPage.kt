package com.example.loginmultiplatform.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.loginmultiplatform.viewmodel.LoginViewModel
import com.example.loginmultiplatform.viewmodel.StudentAnnouncementViewModel

@Composable
expect fun StudentAnnouncementPage(loginViewModel: LoginViewModel, studentAnnouncementViewModel: StudentAnnouncementViewModel, navController: NavController)