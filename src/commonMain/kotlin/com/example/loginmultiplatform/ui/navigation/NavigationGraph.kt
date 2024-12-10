package com.example.loginmultiplatform.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.loginmultiplatform.ui.*
import com.example.loginmultiplatform.viewmodel.AttendanceViewModel
import com.example.loginmultiplatform.viewmodel.LoginViewModel

@Composable
fun NavigationGraph(
    loginViewModel: LoginViewModel,
    attendanceViewModel: AttendanceViewModel
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login_screen"
    ) {
        composable("login_screen") {
            LoginScreen(viewModel = loginViewModel, navController = navController)
        }

        composable("student_dashboard") {
            StudentDashboard(navController = navController, attendanceViewModel = attendanceViewModel, loginViewModel = loginViewModel)
        }

        composable("attendance_screen/{studentId}/{classId}",
            arguments = listOf(
                navArgument("studentId") { type = NavType.IntType },
                navArgument("classId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getInt("studentId")
            val classId = backStackEntry.arguments?.getInt("classId")
            if (studentId != null && classId != null) {
                AttendanceScreen(
                    viewModel = attendanceViewModel,
                    studentId = studentId,
                    classId = 1,
                    navController = navController
                )
            } else {
                navController.popBackStack()
            }
        }
    }
}