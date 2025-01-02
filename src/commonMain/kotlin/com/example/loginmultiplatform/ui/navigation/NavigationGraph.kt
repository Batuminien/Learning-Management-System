package com.example.loginmultiplatform.ui.navigation

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.loginmultiplatform.ui.*
import com.example.loginmultiplatform.ui.components.BottomNavigationBar
import com.example.loginmultiplatform.ui.components.TopBar
import com.example.loginmultiplatform.viewmodel.AdministratorAnnouncementsViewModel
import com.example.loginmultiplatform.viewmodel.AttendanceViewModel
import com.example.loginmultiplatform.viewmodel.LoginViewModel
import com.example.loginmultiplatform.viewmodel.ProfilePhotoViewModel
import com.example.loginmultiplatform.viewmodel.StudentAnnouncementViewModel
import com.example.loginmultiplatform.viewmodel.TeacherAssignmentViewModel
import com.example.loginmultiplatform.viewmodel.TeacherAttendanceViewModel

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun NavigationGraph(
    loginViewModel: LoginViewModel,
    attendanceViewModel: AttendanceViewModel,
    teacherAttendanceViewModel: TeacherAttendanceViewModel
) {
    val navController = rememberNavController()

    val studentAnnouncementViewModel = StudentAnnouncementViewModel()
    val teacherAnnouncementViewModel = AdministratorAnnouncementsViewModel()
    val teacherAssignmentViewModel = TeacherAssignmentViewModel()
    val profilePhotoViewModel: ProfilePhotoViewModel = remember { ProfilePhotoViewModel() }

    val userId by loginViewModel.id.collectAsState()
    val username by loginViewModel.username.collectAsState()
    val role by loginViewModel.role.collectAsState()
    val studentInfo by profilePhotoViewModel.studentInfo.collectAsState()

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    val noBarRoutes = listOf("splash_screen", "login_screen")
    val showBars = currentRoute !in noBarRoutes

    LaunchedEffect(userId) {
        userId?.let { profilePhotoViewModel.fetchStudentInfo(it) }
    }

    val classId = studentInfo?.classId

    Scaffold(
        topBar = {
            if (showBars) {
                TopBar(
                    userName = username,
                    userId = userId ?: -1,
                    onSettingsClick = { },
                    onProfileClick = {
                        val photoUrl = "default_url"
                        navController.navigate("profile/${userId ?: 0}/$photoUrl")
                    },
                    navController = navController
                )
            }
        },
        bottomBar = {
            if (showBars) {
                BottomNavigationBar(
                    pagerState = null,
                    navController = navController
                )
            }
        }
    ) {  innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "splash_screen",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("splash_screen") {
                SplashScreen(navController)
            }
            composable("login_screen") {
                LoginScreen(viewModel = loginViewModel, navController = navController)
            }

            composable("dashboard_page") {
                if (role == "ROLE_STUDENT") {
                    StudentDashboard(navController = navController, attendanceViewModel = attendanceViewModel, loginViewModel = loginViewModel)
                } else if (role == "ROLE_TEACHER") {
                    TeacherDashboard(loginViewModel = loginViewModel, teacherAttendanceViewModel = TeacherAttendanceViewModel(), navController = navController)
                } else if (role == "ROLE_COORDINATOR") {
                    CoordinatorDashboard(studentViewModel = attendanceViewModel, teacherAttendanceViewModel = teacherAttendanceViewModel, loginViewModel = loginViewModel, navController = navController)
                } else if (role == "ROLE_ADMIN") {
                    AdminDashboard(loginViewModel = loginViewModel, studentViewModel = attendanceViewModel, teacherAttendanceViewModel = teacherAttendanceViewModel, navController = navController)
                }
            }

            composable(
                route = "profile/{userId}/{profilePhotoUrl}",
                arguments = listOf(
                    navArgument("userId") { type = NavType.IntType },
                    navArgument("profilePhotoUrl") { type = NavType.StringType; defaultValue = "default_url" }
                )
            ) {
                    backStackEntry ->
                val userId = backStackEntry.arguments?.getInt("userId") ?: 0
                val profilePhotoUrl = backStackEntry.arguments?.getString("profilePhotoUrl") ?: "default_url"

                ProfilePage(
                    loginViewModel,
                    profilePhotoUrl,
                    userId,
                    navController
                )
            }

            composable("announcements_screen") {
                if (role == "ROLE_STUDENT") {
                    StudentAnnouncementPage(loginViewModel, studentAnnouncementViewModel, navController)
                } else {
                    TeacherAnnouncementPage(loginViewModel, teacherAnnouncementViewModel, teacherAttendanceViewModel, studentAnnouncementViewModel, navController)
                }
            }

            composable("attendance_screen") {
                if (role == "ROLE_STUDENT" && userId != null && classId != null) {
                    AttendanceScreen(attendanceViewModel, navController, userId!!, classId!!)
                } else if (role == "ROLE_TEACHER") {
                    TeacherAttendanceScreen(attendanceViewModel, teacherAttendanceViewModel, navController, userId)
                } else if (role == "ROLE_COORDINATOR") {
                    CoordinatorAttendanceScreen(attendanceViewModel, teacherAttendanceViewModel, navController)
                } else if (role == "ROLE_ADMIN") {
                    AdminAttendanceScreen(attendanceViewModel, teacherAttendanceViewModel, navController)
                }
            }

            composable("homework_screen") {
                if (role == "ROLE_TEACHER") {
                    username?.let { it1 ->
                        TeacherHomeworkPage("odev", teacherAssignmentViewModel, userId,
                            it1
                        )
                    }
                }
            }
        }
    }
}
