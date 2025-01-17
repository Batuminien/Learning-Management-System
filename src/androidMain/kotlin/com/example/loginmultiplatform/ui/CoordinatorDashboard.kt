package com.example.loginmultiplatform.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.loginmultiplatform.viewmodel.AdministratorAnnouncementsViewModel
import com.example.loginmultiplatform.viewmodel.AttendanceViewModel
import com.example.loginmultiplatform.viewmodel.LoginViewModel
import com.example.loginmultiplatform.viewmodel.StudentAnnouncementViewModel
import com.example.loginmultiplatform.viewmodel.TeacherAttendanceViewModel

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.example.loginmultiplatform.viewmodel.TeacherAssignmentViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import com.example.loginmultiplatform.model.StudentExamResultsResponses
import com.example.loginmultiplatform.model.TeacherAssignmentResponse
import com.example.loginmultiplatform.viewmodel.CourseScheduleViewModel
import com.example.loginmultiplatform.viewmodel.StudentPastExamResultsViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
actual fun CoordinatorDashboard(studentViewModel: AttendanceViewModel, teacherAttendanceViewModel: TeacherAttendanceViewModel, loginViewModel: LoginViewModel, navController: NavController) {
    val pagerState = rememberPagerState(initialPage = 1, pageCount = { 4 })
    val username by loginViewModel.username.collectAsState()
    val userId by loginViewModel.id.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val administratorAnnouncementsViewModel = AdministratorAnnouncementsViewModel()
    val studentAnnouncementViewModel = StudentAnnouncementViewModel()

    username?.let { CoordinatorDashboardPage(it, userId) }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CoordinatorDashboardPage(username: String, userId : Int?) {
    var isExpended by remember { mutableStateOf(false) }

    val studentExamViewModel = StudentPastExamResultsViewModel()

    val studentAnnouncementViewModel = StudentAnnouncementViewModel()

    val courseScheduleViewModel = CourseScheduleViewModel()

    val teacherAssignmentViewModel = TeacherAssignmentViewModel()


    val courseSchedule by courseScheduleViewModel.courseSchedule.collectAsState()

    val announcements by studentAnnouncementViewModel.announcementUser.collectAsState()

    val PastExams by studentExamViewModel.pastExams.collectAsState()

    val homeworks by teacherAssignmentViewModel.teacherAssignments.collectAsState()

    val classes by teacherAssignmentViewModel.teacherClasses.collectAsState()

    val courseClasses by courseScheduleViewModel.classes.collectAsState()

    val courses by courseScheduleViewModel.courses.collectAsState()

    val Teachers by courseScheduleViewModel.allTeachers.collectAsState()

    var classId by remember { mutableStateOf(-1) }

    var courseId by remember { mutableStateOf(-1) }

    var teacherId by remember { mutableStateOf(-1) }

    var classIdAssignment by remember { mutableStateOf(-1) }



    val bottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()
    val options = listOf("Option 1", "Option 2", "Option 3", "Option 1", "Option 2", "Option 3")


    var selectedOption by remember { mutableStateOf("") }

    var selectedClass by remember { mutableStateOf("") }

    var selectedClassAssignment by remember { mutableStateOf("") }

    var selectedCourse by remember { mutableStateOf("") }

    var selectedStudent by remember { mutableStateOf("") }

    var openBottomSheetState = remember { mutableStateOf(false) }

    var selectedTeacher by remember { mutableStateOf("") }


    var bottomSheetTitle by remember { mutableStateOf<String>("Select an Option") }


    var choosed = remember { mutableStateOf("") }

    LaunchedEffect(Unit){
        if (userId != null) {
            studentExamViewModel.dashboard(studentId = userId.toLong())
            //courseScheduleViewModel.getTeacherSchedule(userId.toLong())
            courseScheduleViewModel.getAllTeachers()
            studentAnnouncementViewModel.getAnnouncementByUserId(userId)
            teacherAssignmentViewModel.fetchTeacherClasses()
            courseScheduleViewModel.getTeacherCourses(userId)

        }
    }







    MaterialTheme {


        ModalBottomSheetLayout(
            sheetState = bottomSheetState,
            sheetContent = {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(bottomSheetTitle, style = MaterialTheme.typography.h6)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier.height(150.dp)
                    ){

                        if (choosed.value == "Class"){
                            println(classes)
                            classes.forEach { option ->
                                item{
                                    Text(
                                        text = option.name,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {


                                                selectedClass = option.name
                                                classId = option.id
                                                selectedStudent = ""
                                                scope.launch { bottomSheetState.hide() }
                                            }
                                            .padding(8.dp)
                                    )
                                }

                            }
                        }else if (choosed.value == "Student") {
                            classes.find { it.id == classId }?.studentIdAndNames?.values?.forEach { option ->
                                item{
                                    Text(
                                        text = option,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedStudent = option

                                                classes.find { it.id == classId }!!.studentIdAndNames.forEach{ map ->
                                                    if (map.value == option){
                                                        studentExamViewModel.fetchStudentPastExams(map.key.toLong())
                                                    }
                                                }

                                                scope.launch { bottomSheetState.hide() }
                                            }
                                            .padding(8.dp)
                                    )
                                }

                            }
                        }else if (choosed.value == "Course"){
                            courses.forEach { option ->
                                item{
                                    Text(
                                        text = option.name,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {


                                                selectedCourse = option.name
                                                courseId = option.id
                                                selectedClassAssignment = ""
                                                if (userId != null) {
                                                    courseScheduleViewModel.getTeacherCourseClass(option.id, userId)
                                                }
                                                scope.launch { bottomSheetState.hide() }
                                            }
                                            .padding(8.dp)
                                    )
                                }

                            }
                        }else if (choosed.value == "Teacher"){
                            Teachers.forEach { option ->
                                item{
                                    Text(
                                        text = option.username,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {


                                                selectedTeacher = option.username
                                                teacherId = option.id
                                                if (teacherId != null) {
                                                    courseScheduleViewModel.getTeacherSchedule(teacherId.toLong())
                                                }
                                                scope.launch { bottomSheetState.hide() }
                                            }
                                            .padding(8.dp)
                                    )
                                }

                            }
                        }else {
                            courseClasses.forEach { option ->
                                item{
                                    Text(
                                        text = option.name,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {


                                                selectedClassAssignment = option.name
                                                classIdAssignment = option.id

                                                if (userId != null) {
                                                    teacherAssignmentViewModel.fetchTeacherAssignments(userId, classIdAssignment, courseId, "", true)
                                                }
                                                scope.launch { bottomSheetState.hide() }
                                            }
                                            .padding(8.dp)
                                    )
                                }

                            }
                        }

                    }

                }
            }
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()
                        .background(color = Color.White)
                ) {
                    // Main Content

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {

                            Card(
                                modifier = Modifier.fillMaxWidth().height(450.dp).background(Color.White),
                                shape = RoundedCornerShape(12.dp),
                                elevation = 5.dp
                            ){
                                Column(
                                    modifier = Modifier.fillMaxSize()
                                ){

                                    Card(
                                        modifier = Modifier.fillMaxWidth().height(80.dp).padding(12.dp),
                                        elevation = 5.dp,
                                        shape = RoundedCornerShape(8.dp)
                                    ){
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,

                                            ){
                                            Spacer(modifier = Modifier.width(25.dp))
                                            Text(
                                                text = "Öğretmen :",
                                                modifier = Modifier.fillMaxWidth(0.3f),
                                                fontSize = 13.sp
                                            )
                                            Button(
                                                onClick = {
                                                    choosed.value = "Teacher"
                                                    scope.launch { bottomSheetState.show() }

                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.fillMaxSize().padding(end = 12.dp, top = 6.dp, bottom = 6.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    backgroundColor = Color.White
                                                )
                                            ){
                                                Row(
                                                    modifier = Modifier.fillMaxSize(),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ){
                                                    Text (
                                                        text = if (selectedTeacher == "" ) "Öğretmen Seçiniz" else selectedClass

                                                    )
                                                }

                                            }
                                        }
                                    }


                                    WeeklyScheduleSection(courseSchedule, "ROLE_TEACHER")
                                }

                            }




                        }
                        item {
                            TeacherExamSection(
                                PastExams = PastExams,
                                openBottomSheet = { scope.launch { bottomSheetState.show() } },
                                selectedOption,
                                bottomSheetTitle,
                                selectedClass,
                                selectedStudent,
                                choosed

                            )

                        }
                        item { TeacherAssignmentSection(fetchAllAssignment = {
                            if (userId != null) {
                                teacherAssignmentViewModel.fetchTeacherAssignments(userId, null, null , "", true)
                            }
                        } ,openBottomSheet = { scope.launch { bottomSheetState.show() } }, selectedCourse, selectedClassAssignment, homeworks, choosed) }
                        item { AnnouncementsSection(isExpended, announcements) }
                    }

                }
                if (username != null) {
                    SideDrawer(isExpended, username) { isExpended = false }
                }
            }


        }

    }
}

@Composable
fun CoordinatorHomeworkPage(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            fontFamily = customFontFamily
        )
    }
}

