// File: src/androidMain/kotlin/com/example/loginmultiplatform/MainActivity.kt

package com.example.loginmultiplatform

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.example.loginmultiplatform.ui.navigation.NavigationGraph
import com.example.loginmultiplatform.viewmodel.AttendanceViewModel
import com.example.loginmultiplatform.viewmodel.LoginViewModel
import com.example.loginmultiplatform.viewmodel.TeacherAttendanceViewModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize AppContext if using the singleton approach
        AppContext.context = this.applicationContext

        setContent {
            val loginViewModel = LoginViewModel()
            val attendanceViewModel = AttendanceViewModel()
            val teacherAttendanceViewModel = TeacherAttendanceViewModel()

            NavigationGraph(
                loginViewModel = loginViewModel,
                attendanceViewModel = attendanceViewModel,
                teacherAttendanceViewModel = teacherAttendanceViewModel
            )
        }
    }
}
