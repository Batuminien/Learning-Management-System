package com.example.loginmultiplatform.ui.navigation

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.example.loginmultiplatform.viewmodel.StudentPastExamResultsViewModel
import com.example.loginmultiplatform.viewmodel.TeacherAssignmentViewModel
import com.example.loginmultiplatform.viewmodel.TeacherAttendanceViewModel

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "NewApi")
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
    val studentPastExamResultsViewModel = StudentPastExamResultsViewModel()
    val profilePhotoViewModel: ProfilePhotoViewModel = remember { ProfilePhotoViewModel() }


    val userId by loginViewModel.id.collectAsState()
    val username by loginViewModel.username.collectAsState()
    val role by loginViewModel.role.collectAsState()
    val studentInfo by profilePhotoViewModel.studentInfo.collectAsState()
    val profilePhotoUrl by profilePhotoViewModel.profilePhotoUrl.collectAsState()

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    val noBarRoutes = listOf("splash_screen", "login_screen")
    val showBars = currentRoute !in noBarRoutes

    LaunchedEffect(userId) {
        userId?.let {
            profilePhotoViewModel.fetchStudentInfo(it)
            profilePhotoViewModel.fetchProfilePhoto(it)
        }

    }

    val classId = studentInfo?.classId
    Log.e("Navigation", "Navigating to profile/${userId ?: 0}/${profilePhotoUrl}")

    Scaffold(
        topBar = {
            if (showBars) {
                val encodeUrl = Uri.encode(profilePhotoUrl)
                TopBar(
                    userName = username,
                    userId = userId ?: -1,
                    onSettingsClick = { },
                    onProfileClick = {
                        navController.navigate("profile/${userId ?: 0}/${encodeUrl ?: "default_url"}")
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
                if (role == "ROLE_STUDENT" && classId != null) {
                    StudentDashboard(navController = navController, attendanceViewModel = attendanceViewModel, loginViewModel = loginViewModel, studentId = userId, classId = classId)
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
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getInt("userId") ?: 0
                val rawPhotoUrl = backStackEntry.arguments?.getString("profilePhotoUrl") ?: "default_url"
                val profilePhotoUrl = Uri.decode(rawPhotoUrl)

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

            composable("past_exams_screen") {
                if (role == "ROLE_STUDENT" && userId != null && classId != null) {
                    StudentPastExamPage(loginViewModel, navController, studentPastExamResultsViewModel)
                }
            }
        }
    }
}
