package com.example.loginmultiplatform.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.example.loginmultiplatform.viewmodel.AdministratorAnnouncementsViewModel
import com.example.loginmultiplatform.viewmodel.AttendanceViewModel
import com.example.loginmultiplatform.viewmodel.LoginViewModel
import com.example.loginmultiplatform.viewmodel.StudentAnnouncementViewModel
import com.example.loginmultiplatform.viewmodel.TeacherAssignmentViewModel
import com.example.loginmultiplatform.viewmodel.TeacherAttendanceViewModel
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
actual fun TeacherDashboard(loginViewModel: LoginViewModel, studentViewModel: AttendanceViewModel, teacherAttendanceViewModel: TeacherAttendanceViewModel, teacherHomeworkViewModel : TeacherAssignmentViewModel, navController: NavController) {
    val pagerState = rememberPagerState(initialPage = 1, pageCount = { 4 })
    val username by loginViewModel.username.collectAsState()
    val teacherId by loginViewModel.id.collectAsState()


    val coroutineScope = rememberCoroutineScope()
    val teacherAnnouncementViewModel = AdministratorAnnouncementsViewModel()
    val studentAnnouncementViewModel = StudentAnnouncementViewModel()

    username?.let { teacherId?.let { it1 -> TeacherDashboardPage(it, it1) } }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TeacherDashboardPage(username: String, teacherId : Int) {
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

    var classId by remember { mutableStateOf(-1) }

    var courseId by remember { mutableStateOf(-1) }

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


    var bottomSheetTitle by remember { mutableStateOf<String>("Select an Option") }


    var choosed = remember { mutableStateOf("") }

    LaunchedEffect(Unit){
        if (teacherId != null) {
            studentExamViewModel.dashboard(studentId = teacherId.toLong())
            courseScheduleViewModel.getTeacherSchedule(teacherId.toLong())
            studentAnnouncementViewModel.getAnnouncementByUserId(teacherId)
            teacherAssignmentViewModel.fetchTeacherClasses()
            courseScheduleViewModel.getTeacherCourses(teacherId)

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
                                                courseScheduleViewModel.getTeacherCourseClass(option.id, teacherId)
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

                                                teacherAssignmentViewModel.fetchTeacherAssignments(teacherId, classIdAssignment, courseId, "", true)
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
                        item { WeeklyScheduleSection(courseSchedule, "ROLE_TEACHER") }
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
                        item { TeacherAssignmentSection(fetchAllAssignment = {teacherAssignmentViewModel.fetchTeacherAssignments(teacherId, null, null , "", true) } ,openBottomSheet = { scope.launch { bottomSheetState.show() } }, selectedCourse, selectedClassAssignment, homeworks, choosed) }
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
fun TeacherAssignmentSection(fetchAllAssignment: () -> Unit , openBottomSheet: () -> Unit ,selectedCourse : String, selectedClass : String, homeworks : List<TeacherAssignmentResponse>, choosed: MutableState<String>){
    Card(
        modifier = Modifier.fillMaxWidth().height(740.dp),
        elevation = 5.dp,
        shape = RoundedCornerShape(12.dp)
    ){
        Column(
            modifier = Modifier.fillMaxSize().background(Color.White, RoundedCornerShape(12.dp))
        ){
            Spacer (modifier = Modifier.height(15.dp))


            // Ders seçimi
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
                        text = "Ders :",
                        modifier = Modifier.fillMaxWidth(0.3f)
                    )
                    Button(
                        onClick = {
                            //TODO
                            choosed.value = "Course"
                            openBottomSheet()


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
                                text = if (selectedCourse == "" ) "Ders Seçiniz" else selectedCourse

                            )
                        }

                    }
                }
            }


            Spacer(modifier = Modifier.height(25.dp))


            // Sınıf seçimi
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
                        text = "Sınıf :",
                        modifier = Modifier.fillMaxWidth(0.3f)
                    )
                    Button(
                        onClick = {
                            //TODO
                            if ( selectedCourse != ""){
                                choosed.value = "Assignment"
                                openBottomSheet()
                            }

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
                                text = if (selectedClass == "" ) "Sınıf Seçiniz" else selectedClass

                            )
                        }

                    }
                }
            }


            Spacer (modifier = Modifier.height(15.dp))

            Card(
                modifier = Modifier.fillMaxWidth().height(80.dp).padding(12.dp).clickable{
                    fetchAllAssignment()
                },
                elevation = 5.dp,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tüm ödevlerim",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }


            HomeworkSectionTeacher(homeworks)
        }

    }
}


@Composable
fun TeacherExamSection(PastExams : List<StudentExamResultsResponses>, openBottomSheet: () -> Unit,
                       selectedOption : String, bottomSheetTitle : String, selectedClass : String,
                       selectedStudent : String, choosed : MutableState<String>

){
    Card(
        modifier = Modifier.fillMaxWidth().height(680.dp),
        elevation = 5.dp,
        shape = RoundedCornerShape(12.dp)
    ){
        Column(
            modifier = Modifier.fillMaxSize().background(Color.White, RoundedCornerShape(12.dp))
        ){
            Spacer (modifier = Modifier.height(15.dp))


            // Sınıf seçimi
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
                        text = "Sınıf :",
                        modifier = Modifier.fillMaxWidth(0.3f)
                    )
                    Button(
                        onClick = {
                            choosed.value = "Class"
                            openBottomSheet()

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
                                text = if (selectedClass == "" ) "Sınıf Seçiniz" else selectedClass

                            )
                        }

                    }
                }
            }


            Spacer(modifier = Modifier.height(25.dp))


            // Öğrenci seçimi
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
                        text = "Öğrenci :",
                        modifier = Modifier.fillMaxWidth(0.3f)
                    )
                    Button(
                        onClick = {
                            if (selectedClass != ""){
                                choosed.value = "Student"
                                openBottomSheet()
                            }


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
                                text = if (selectedStudent == "" ) "Öğrenci Seçiniz" else selectedStudent

                            )
                        }

                    }
                }
            }


            Spacer (modifier = Modifier.height(15.dp))
            ExamsSection(PastExams, "ROLE_TEACHER")
        }

    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BottomSheetExample() {
    val bottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()
    val options = listOf("Option 1", "Option 2", "Option 3")
    var selectedOption by remember { mutableStateOf("") }

    ModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetContent = {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Select an option", style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(8.dp))
                options.forEach { option ->
                    Text(
                        text = option,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedOption = option
                                scope.launch { bottomSheetState.hide() }
                            }
                            .padding(8.dp)
                    )
                }
            }
        }
    ) {
        Button(onClick = {
            scope.launch { bottomSheetState.show() }
        }) {
            Text("Show Bottom Sheet")
        }
        if (selectedOption.isNotEmpty()) {
            Text("Selected Option: $selectedOption")
        }
    }
}
