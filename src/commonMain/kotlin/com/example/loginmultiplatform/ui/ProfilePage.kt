package com.example.loginmultiplatform.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.loginmultiplatform.viewmodel.LoginViewModel

@Composable
expect fun ProfilePage(loginViewModel: LoginViewModel, profilePhotoUrl: String?, userId: Int, navController: NavController)