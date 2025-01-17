package com.example.loginmultiplatform.ui

import androidx.compose.runtime.Composable
import com.example.loginmultiplatform.viewmodel.LoginViewModel
import com.example.loginmultiplatform.viewmodel.StudentAssignmentViewModel

@Composable
expect fun StudentHomeworkPage(studentViewModel : StudentAssignmentViewModel, studentId: Int?)