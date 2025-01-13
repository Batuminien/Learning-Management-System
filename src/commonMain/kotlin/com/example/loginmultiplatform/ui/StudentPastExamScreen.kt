package com.example.loginmultiplatform.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.loginmultiplatform.viewmodel.LoginViewModel
import com.example.loginmultiplatform.viewmodel.StudentPastExamResultsViewModel

@Composable
expect fun StudentPastExamPage (loginViewModel: LoginViewModel, navController: NavController, studentPastExamViewModel : StudentPastExamResultsViewModel)