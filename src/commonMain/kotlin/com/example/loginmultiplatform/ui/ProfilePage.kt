package com.example.loginmultiplatform.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.loginmultiplatform.viewmodel.LoginViewModel
import com.example.loginmultiplatform.viewmodel.ProfilePhotoViewModel

@Composable
expect fun ProfilePage(loginViewModel: LoginViewModel, profilePhotoViewModel: ProfilePhotoViewModel, userId: Int, navController: NavController)