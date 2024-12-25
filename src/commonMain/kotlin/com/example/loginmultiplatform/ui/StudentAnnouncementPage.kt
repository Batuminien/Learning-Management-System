package com.example.loginmultiplatform.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.loginmultiplatform.viewmodel.LoginViewModel

@Composable
expect fun StudentAnnouncementPage(loginViewModel: LoginViewModel, navController: NavController)