package com.example.loginmultiplatform.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults.buttonColors
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import com.example.loginmultiplatform.R
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.*
import kotlin.math.pow
import kotlin.math.round
import androidx.navigation.NavController
import com.example.loginmultiplatform.model.CourseSchedule
import com.example.loginmultiplatform.model.StudentAnnouncementResponse
import com.example.loginmultiplatform.model.StudentDashboard
import com.example.loginmultiplatform.model.StudentExamResultsResponses
import com.example.loginmultiplatform.model.StudentSubmission
import com.example.loginmultiplatform.model.TeacherAssignmentResponse
import com.example.loginmultiplatform.viewmodel.AttendanceViewModel
import com.example.loginmultiplatform.viewmodel.CourseScheduleViewModel
import com.example.loginmultiplatform.viewmodel.LoginViewModel
import com.example.loginmultiplatform.viewmodel.StudentAnnouncementViewModel
import com.example.loginmultiplatform.viewmodel.StudentPastExamResultsViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
actual fun StudentDashboard(navController: NavController, loginViewModel: LoginViewModel, attendanceViewModel: AttendanceViewModel, studentId : Int?, classId : Int?) {
    val username by loginViewModel.username.collectAsState()

    DashboardPage(username, studentId, classId)
}

@Composable
fun DashboardPage(username: String?, studentId: Int? , classId: Int?) {
    var isExpended by remember { mutableStateOf(false) }

    val studentExamViewModel = StudentPastExamResultsViewModel()

    val studentAnnouncementViewModel = StudentAnnouncementViewModel()

    val courseScheduleViewModel = CourseScheduleViewModel()

    val courseSchedule by courseScheduleViewModel.courseSchedule.collectAsState()

    val announcements by studentAnnouncementViewModel.announcement.collectAsState()

    val PastExams by studentExamViewModel.pastExams.collectAsState()

    val homeworks by studentExamViewModel.homeworks.collectAsState()



    LaunchedEffect(Unit){
        if (studentId != null) {
            studentExamViewModel.fetchStudentPastExams(studentId = studentId.toLong())
            studentExamViewModel.dashboard(studentId = studentId.toLong())
            courseScheduleViewModel.getStudentSchedule(studentId.toLong())
            if(classId != null){
                studentAnnouncementViewModel.fetchAnnouncementsByClassId(classId!!)
                println("$classId")
            }else {
                println("neden ?")
            }

        }
    }



    MaterialTheme {
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
                    item { WeeklyScheduleSection(courseSchedule, "ROLE_STUDENT") }
                    item { ExamsSection(PastExams, "ROLE_STUDENT") }
                    item { HomeworkSection(homeworks) }
                    item { AnnouncementsSection(isExpended, announcements) }
                }
            }
            if (username != null) {
                SideDrawer(isExpended, username) { isExpended = false }
            }
        }
    }
}



@Composable
fun HomeworkPage(title: String) {
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

//========================= Data Classes =========================//
data class Homeworks(val CourseName: String, val CourseConcept: String, val FinishDate: String, val Statue: Boolean)
data class LessonProgram(val start_hour: Float, val end_hour: Float, val Lday: String, val name: String)
data class Announcements(val title: String, val description: String, val state: Boolean)
data class Exams(val personal: Float, val overall: Float, val examName: String, val examType: Boolean)

//========================= Dummy Data =========================//
fun DummyLessons() = listOf(
    LessonProgram(10.30f, 12.20f, "Pzt", "Programming Languages"),
    LessonProgram(10.30f, 12.20f, "Salı", "Introduction to Algorithm Design"),
    LessonProgram(13.30f, 16.30f, "Salı", "Software Engineering"),
    LessonProgram(14.30f, 17.30f, "Salı", "Economy"),
    LessonProgram(10.30f, 11.20f, "Çrş", "Introduction to Algorithm Design"),
    LessonProgram(14.30f, 16.30f, "Çrş", "Programming Languages"),
    LessonProgram(9.00f, 9.15f, "Prş", "Circuit Lab"),
    LessonProgram(13.30f, 16.30f, "Prş", "Computer Organization")
)

fun DummyHomeworks() = listOf(
    Homeworks("Türkçe", "Cümlede Anlam", "13/10", false),
    Homeworks("Tyt Matematik", "Çarpanlara Ayırma", "15/10", true),
    Homeworks("TYT Kimya", "Element Tablosu", "17/10", true),
    Homeworks("AYT Fizik", "Elektromanyetik Alan", "22/09", false)
)

fun DummyAnnouncements() = listOf(
    Announcements("Giriş saati", "Okula son giriş saati sabah sekizdir", false),
    Announcements("Sigara Kullanımı", "Yangın merdiveninde sigara içmek yasaktır", true),
    Announcements("Değerlendirme test sonuçları", "İkinci değerlendirme test sonuçları yayınlandı", true)
)

fun DummyExams() = listOf(
    Exams(100.25f, 105.75f, "TYT Özdebir - 1", true),
    Exams(76f, 80.50f, "TYT Özdebir - 2", true),
    Exams(40f, 46f, "AYT Üçdörtbeş - 1", false),
    Exams(90f, 85f, "TYT Özdebir - 3", true),
    Exams(110f, 113.25f, "TYT Üçdörtbeş - 1", true)
)

//========================= Utility Functions =========================//

@SuppressLint("NewApi")
fun compareDates(dateString: String) : Int {
    // Define the date format
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // Parse the string date into LocalDate
    val parsedDate = LocalDate.parse(dateString, formatter)

    // Get the current date
    val currentDate = LocalDate.now()

    // Compare the dates
    when {
        parsedDate.isBefore(currentDate) -> return -1
        parsedDate.isAfter(currentDate) -> return 1
        else -> return -1
    }
}


fun timeToFloat(timeString: String): Float {
    val parts = timeString.split(":")
    val hours = parts[0].toInt()
    val minutes = parts[1].toInt()

    // Convert minutes to the fractional part of the hour
    return hours + minutes / 100f
}


fun formatFloat(value: Float, decimals: Int = 2): String {
    val factor = 10.0.pow(decimals)
    val roundedValue = round(value * factor) / factor
    val parts = roundedValue.toString().split(".")
    val integerPart = parts[0]
    val decimalPart = parts.getOrElse(1) { "0" }.padEnd(decimals, '0')
    return "$integerPart.$decimalPart"
}

fun getHomeworkList(homeworkList: List<Homeworks>): List<Homeworks> =
    if (homeworkList.size <= 4) homeworkList else homeworkList.subList(0, 4)

fun getAnnouncementList(announcementList: List<Announcements>): List<Announcements> =
    if (announcementList.size <= 4) announcementList else announcementList.subList(0, 4)

fun get_lesson_color(name: String): Color = when (name) {
    "Programming Languages" -> Color.Red
    "Introduction to Algorithm Design" -> Color.Green
    "Software Engineering" -> Color.Cyan
    "Economy" -> Color.Yellow
    "Circuit Lab" -> Color(0xFFA020F0)
    "Computer Organization" -> Color(0xFFFFA500)
    else -> Color.White
}

//========================= Composable UI Components =========================//
@Composable
fun GradientDivider() {
    Box(
        modifier = Modifier.fillMaxWidth().height(1.dp).background(
            Brush.horizontalGradient(
                colors = listOf(Color.Gray.copy(alpha = 0.8f), Color.Transparent)
            )
        )
    )
}

@Composable
fun SideBarButton(text: String, onClick: () -> Unit = {}) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        colors = buttonColors(backgroundColor = Color.White)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Text(
                text = text,
                modifier = Modifier.fillMaxWidth(0.8f).align(Alignment.CenterVertically),
                fontFamily = customFontFamily
            )
            Box(modifier = Modifier.fillMaxSize()) {
                Icon(
                    painter = painterResource(R.drawable.sidemenu),
                    contentDescription = "Side Menu button",
                    modifier = Modifier.align(Alignment.Center).size(10.dp)
                )
            }
        }
    }
}


@Composable
fun HomeworkDisplay(content: String) {

    Box (
        modifier = Modifier.width(125.dp).height(60.dp)
    ){
        Text(
            text = content,
            modifier = Modifier.align(Alignment.Center),
            fontSize = 13.sp
        )
    }
}


@Composable
fun AnnouncementsRow(item: StudentAnnouncementResponse, isExpanded: Boolean) {

    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = 3.dp,
        backgroundColor = if (!item.read) Color(0xFFD8EBF5) else Color(0xFFF2F2F2)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                .height(70.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxHeight().fillMaxWidth(0.80f)
            ) {
                Text(text = item.title, lineHeight = 15.sp, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = customFontFamily)
                Text(text = item.content, lineHeight = 15.sp, fontSize = 10.sp, fontFamily = customFontFamily)
            }
            Box(modifier = Modifier.fillMaxSize()) {
                Icon(
                    painter = painterResource(if (item.read) R.drawable.read else R.drawable.un_read),
                    contentDescription = if (item.read) "read Mail icon" else "un_read Mail icon",
                    modifier = Modifier.size(25.dp).align(Alignment.Center),
                    tint = Color.Unspecified
                )
            }
        }
    }
}

@Composable
fun Bars(personal: Float, overall: Float, examName: String, examType: String) {
    val divisor = if (examType == "TYT") 120f else if ( examType == "LGS" ) 90f else 80f
    val personalRatio = personal / divisor
    val overallRatio = overall / divisor

    Column(modifier = Modifier.fillMaxHeight().width(80.dp)) {
        Row(modifier = Modifier.width(80.dp).height(175.dp), verticalAlignment = Alignment.Bottom) {
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxHeight(personalRatio)
                    .width(25.dp)
                    .background(Color(0xFF28a745), RoundedCornerShape(3.dp))
            )
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxHeight(overallRatio)
                    .width(25.dp)
                    .background(Color(0xFFf76c5e), RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.width(22.dp))
        }

        Row(modifier = Modifier.height(20.dp).fillMaxWidth()) {
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = personal.toString(),
                fontSize = 6.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Black,
                modifier = Modifier.width(25.dp),
                fontFamily = customFontFamily
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = overall.toString(),
                fontSize = 6.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(25.dp),
                fontFamily = customFontFamily
            )
            Spacer(modifier = Modifier.width(22.dp))
        }

        Text(
            text = examType,
            fontSize = 10.sp,
            color = Color.Black,
            textAlign = TextAlign.Center,
            lineHeight = 13.sp,
            fontFamily = customFontFamily,
            modifier = Modifier.width(80.dp)
        )

        Text(
            text = examName,
            fontSize = 10.sp,
            color = Color.Black,
            textAlign = TextAlign.Center,
            lineHeight = 13.sp,
            fontFamily = customFontFamily,
            modifier = Modifier.width(80.dp)
        )
    }
}



//========================= Sections as Composables =========================//
@Composable
fun WeeklyScheduleSection(courseSchedule: List<CourseSchedule>, role : String) {
    //"MONDAY" "TUESDAY" "WEDNESDAY" "THURSDAY" "FRIDAY" "SATURDAY" "SUNDAY"
    val days = listOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY")
    val timeslots = (10..18)


    Card(
        modifier = Modifier.fillMaxWidth().height(400.dp).background(Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = 5.dp
    ){
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(5.dp))
            Text("HAFTALIK DERS PROGRAMI", modifier = Modifier.fillMaxWidth(), fontSize = 16.sp ,textAlign = TextAlign.Center, fontFamily = customFontFamily, fontWeight = FontWeight.Bold)
            Divider(thickness = 3.dp, color = Color.Black, modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end= 10.dp))
            Spacer(modifier = Modifier.height(15.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.width(15.dp))
                    Column(modifier = Modifier.fillMaxHeight().fillMaxWidth(0.2f)) {
                        Row(modifier = Modifier.fillMaxWidth().height(30.dp)) {
                            Text("Hafta", fontSize = 12.sp, fontFamily = customFontFamily)
                        }
                        Row(modifier = Modifier.fillMaxWidth().height(30.dp)) { Text("09.00", fontSize = 12.sp, fontFamily = customFontFamily) }
                        timeslots.forEach { time ->
                            Row(modifier = Modifier.fillMaxWidth().height(30.dp)) {
                                Text("${time}.00", fontSize = 12.sp, fontFamily = customFontFamily)
                            }
                        }
                    }

                    LazyRow(modifier = Modifier.fillMaxSize().padding(end = 10.dp)) {
                        days.forEach { day ->
                            item {
                                Box(modifier = Modifier.fillMaxHeight().width(150.dp)) {
                                    courseSchedule.forEach { schedule ->

                                        //println("start_hour original: ${schedule.startTime}\n start_hour formatted : ${timeToFloat(schedule.startTime)}")

                                        //println("end_hour original: ${schedule.endTime}\n end_hour formatted : ${timeToFloat(schedule.endTime)}")

                                        val start_hour = timeToFloat(schedule.startTime)

                                        val end_hour = timeToFloat(schedule.endTime)

                                        if (day == schedule.dayOfWeek) {
                                            val heightCalc = ((30 * (end_hour.toInt() - start_hour.toInt())) +
                                                    (((end_hour - end_hour.toInt()) - (start_hour - start_hour.toInt())) * 47.5f) +
                                                    ((end_hour.toInt() - start_hour.toInt()) * 1.2f)).dp

                                            val offsetCalc = (42.5 + (29 * ((start_hour - 9.00).toInt()) +
                                                    ((start_hour - start_hour.toInt()) * (1.6f) * 29) +
                                                    (start_hour.toInt() - 9))).dp

                                            Box(
                                                modifier = Modifier
                                                    .height(heightCalc)
                                                    .fillMaxWidth()
                                                    .offset(y = offsetCalc)
                                                    .background(Color(0xFF6d5bfc).copy(alpha = 0.8f), RoundedCornerShape(10.dp))
                                            ) {
                                                Text(
                                                    text = schedule.teacherCourseName + if (role == "ROLE_TEACHER") "\n${schedule.className}" else if (role == "ROLE_STUDENT") "\n${schedule.teacherName}"  else "",
                                                    modifier = Modifier.align(Alignment.Center).fillMaxWidth(),
                                                    fontSize = 10.sp,
                                                    textAlign = TextAlign.Center,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = customFontFamily
                                                )
                                            }
                                        }
                                    }

                                    Column(modifier = Modifier.fillMaxSize()) {
                                        Text(day, fontSize = 10.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontFamily = customFontFamily)
                                        Spacer(modifier = Modifier.height(15.dp))
                                        Divider(modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.0005f)))

                                        timeslots.forEach { _ ->
                                            Spacer(modifier = Modifier.height(29.dp))
                                            Divider(modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.0005f)))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun ExamsSection(PastExams : List<StudentExamResultsResponses>, role : String) {
    val exams = DummyExams()
    var personalAverage = 0f
    var overallAverage = 0f

    if (PastExams.isNotEmpty()){
        PastExams.forEach{ exams ->
            exams.subjectResults.forEach{ net ->
                personalAverage += net.netScore
            }

            overallAverage += exams.pastExam.overallAverage
        }

        personalAverage = personalAverage / PastExams.size.toLong()
        overallAverage = overallAverage / PastExams.size.toLong()
    }else{
        personalAverage = -1f
        overallAverage = -1f
    }


    Card (
        modifier = Modifier.fillMaxWidth().height(450.dp)
        .background(Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = if(role == "ROLE_STUDENT" ) 5.dp else 0.dp

    ){
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(30.dp))
            Text(
                text = "Geçmiş Sınavlar",
                fontWeight = FontWeight.Bold,
                fontFamily = customFontFamily,
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                modifier = Modifier
                .fillMaxWidth()
                .height(25.dp)
            )

            Divider(thickness = 3.dp, color = Color.Black, modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp))
            Spacer(modifier = Modifier.height(20.dp))

            if (PastExams.isNotEmpty()){
                LazyRow(
                    modifier = Modifier.height(240.dp).fillMaxWidth().padding(start = 10.dp, end = 10.dp)
                        .background(Color(0xFFe6eaf8), RoundedCornerShape(20.dp)),
                    verticalAlignment = Alignment.Bottom
                ) {

                    item { Spacer(modifier = Modifier.width(20.dp)) }
                    PastExams.forEach { exam ->
                        item {
                            var personal = 0f
                            exam.subjectResults.forEach{ net ->
                                personal += net.netScore
                            }

                            Bars(personal, exam.pastExam.overallAverage, exam.pastExam.name, exam.pastExam.examType)
                            Spacer(modifier = Modifier.width(20.dp))
                        }
                    }
                }
            }else {
                Box (
                    modifier = Modifier.height(240.dp).fillMaxWidth()
                        .background(Color(0xFFacc5e9), RoundedCornerShape(20.dp))
                ){
                    Text (
                        text = "Şu an gösterilecek bir sınav kaydı bulunmamaktadır",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }


            Spacer(modifier = Modifier.height(25.dp))
            Row(
                modifier = Modifier.fillMaxWidth().height(30.dp).padding(start = 25.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(30.dp)
                        .background(color = Color(0xFF28a745), RoundedCornerShape(5.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.Center).size(25.dp),
                        tint = Color(0xFFFFFFFF)
                    )
                }


                Spacer(modifier = Modifier.width(15.dp))
                Text("Kişisel Ortalama", fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFamily = customFontFamily)
                Spacer(modifier = Modifier.width(40.dp))
                Text( if (personalAverage < 0) "-" else formatFloat(personalAverage, 2), color = Color(0xFF28a745), fontSize = 13.sp, fontFamily = customFontFamily)
            }

            Spacer(modifier = Modifier.height(5.dp))
            Row(
                modifier = Modifier.fillMaxWidth().height(30.dp).padding(start = 25.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(30.dp)
                        .background(color = Color(0xFFf76c5e), RoundedCornerShape(5.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.Center).size(18.dp),
                        tint = Color(0xFFFFFFFF)
                    )
                }
                Spacer(modifier = Modifier.width(15.dp))
                Text("Genel Ortalama", fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFamily = customFontFamily)
                Spacer(modifier = Modifier.width(46.dp))
                Text( if (overallAverage < 0) "-" else  formatFloat(overallAverage, 2), color = Color(0xFFf76c5e), fontSize = 13.sp, fontFamily = customFontFamily)
            }
        }
    }

}

@Composable
fun HomeworkSectionTeacher(homeworks : List<TeacherAssignmentResponse>) {
    val myFontSize = 11.sp

    Spacer(modifier = Modifier.height(20.dp))

    Card (
        modifier = Modifier.fillMaxWidth().height(380.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = 5.dp
    ){
        Column(
            modifier = Modifier.fillMaxSize()
        ){
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Ödevler",
                fontWeight = FontWeight.Bold,
                fontFamily = customFontFamily,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(25.dp)
            )

            Divider(thickness = 3.dp, color = Color.Black, modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp))

            Spacer(modifier = Modifier.height(10.dp))

            if (homeworks.isNotEmpty()){
                LazyRow (
                    modifier = Modifier.fillMaxSize().padding(start = 10.dp, end = 10.dp)
                ) {

                    item {
                        Column(
                            modifier = Modifier.fillMaxHeight().width(((150*4)-25).dp)
                        ){
                            Row(
                                modifier = Modifier.fillMaxWidth().height(45.dp)
                            ){
                                LabelWithSortIcon("Ödev Durumu", myFontSize)
                                Spacer(modifier = Modifier.width(25.dp))
                                LabelWithSortIcon("Ders Adı", myFontSize)
                                Spacer(modifier = Modifier.width(25.dp))
                                LabelWithSortIcon("Başlık", myFontSize)
                                Spacer(modifier = Modifier.width(25.dp))
                                LabelWithSortIcon("Bitiş Tarihi", myFontSize)


                            }

                            Card(
                                modifier = Modifier.padding(start = 1.dp, end = 1.dp).fillMaxWidth().height(60.dp),
                                backgroundColor = Color.White,
                                elevation = 5.dp,
                                shape = RoundedCornerShape(12.dp)
                            ){
                                Row (
                                    modifier = Modifier.fillMaxSize()
                                ){
                                    homeworks.forEach { home ->

                                        Box (
                                            modifier = Modifier.fillMaxHeight().width(125.dp)
                                        ){
                                            Card(
                                                modifier = Modifier.padding(15.dp).fillMaxSize(),
                                                backgroundColor = if (compareDates(home.dueDate) < 0) Color.Green else Color.Red,
                                                shape = RoundedCornerShape(12.dp)
                                            ){
                                                Box (
                                                    modifier = Modifier.fillMaxSize()
                                                ){
                                                    Text(
                                                        text = if (compareDates(home.dueDate) < 0) "Aktif" else "Geç",
                                                        modifier = Modifier.align(Alignment.Center),
                                                        fontSize = 13.sp
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(25.dp))

                                        HomeworkDisplay(home.courseName)
                                        Spacer(modifier = Modifier.width(25.dp))
                                        HomeworkDisplay(home.title)
                                        Spacer(modifier = Modifier.width(25.dp))
                                        HomeworkDisplay(home.dueDate)

                                    }
                                }
                            }


                        }


                    }


                }
            }else{
                Box(
                    modifier = Modifier.fillMaxWidth()
                ){
                    Text (
                        "Henüz bir ödev yok",
                        textAlign = TextAlign.Center
                    )
                }
            }



        }


    }

}

@Composable
fun HomeworkSection(homeworks : List<StudentDashboard>) {

    val myFontSize = 11.sp

    Spacer(modifier = Modifier.height(20.dp))

    Card (
        modifier = Modifier.fillMaxWidth().height(380.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = 5.dp
    ){
        Column(
            modifier = Modifier.fillMaxSize()
        ){
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Ödevler",
                fontWeight = FontWeight.Bold,
                fontFamily = customFontFamily,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(25.dp)
            )

            Divider(thickness = 3.dp, color = Color.Black, modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp))

            Spacer(modifier = Modifier.height(10.dp))

            if (homeworks.isNotEmpty()){
                LazyRow (
                    modifier = Modifier.fillMaxSize().padding(start = 10.dp, end = 10.dp)
                ) {

                    item {
                        Column(
                            modifier = Modifier.fillMaxHeight().width(((150*4)-25).dp)
                        ){
                            Row(
                                modifier = Modifier.fillMaxWidth().height(45.dp)
                            ){
                                LabelWithSortIcon("Ödev Durumu", myFontSize)
                                Spacer(modifier = Modifier.width(25.dp))
                                LabelWithSortIcon("Ders Adı", myFontSize)
                                Spacer(modifier = Modifier.width(25.dp))
                                LabelWithSortIcon("Başlık", myFontSize)
                                Spacer(modifier = Modifier.width(25.dp))
                                LabelWithSortIcon("Bitiş Tarihi", myFontSize)


                            }

                            Card(
                                modifier = Modifier.padding(start = 1.dp, end = 1.dp).fillMaxWidth().height(60.dp),
                                backgroundColor = Color.White,
                                elevation = 5.dp,
                                shape = RoundedCornerShape(12.dp)
                            ){
                                Row (
                                    modifier = Modifier.fillMaxSize()
                                ){
                                    homeworks.forEach { home ->

                                        Box (
                                            modifier = Modifier.fillMaxHeight().width(125.dp)
                                        ){
                                            Card(
                                                modifier = Modifier.padding(15.dp).fillMaxSize(),
                                                backgroundColor = if (home.mySubmission != null) Color.Green else Color.Red,
                                                shape = RoundedCornerShape(12.dp)
                                            ){
                                                Box (
                                                    modifier = Modifier.fillMaxSize()
                                                ){
                                                    Text(
                                                        text = if (home.mySubmission != null) "Teslim" else "Aktif",
                                                        modifier = Modifier.align(Alignment.Center),
                                                        fontSize = 13.sp
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(25.dp))

                                        HomeworkDisplay(home.courseName)
                                        Spacer(modifier = Modifier.width(25.dp))
                                        HomeworkDisplay(home.title)
                                        Spacer(modifier = Modifier.width(25.dp))
                                        HomeworkDisplay(home.dueDate)

                                    }
                                }
                            }


                        }


                    }


                }
            }else{
                Box(
                    modifier = Modifier.fillMaxWidth()
                ){
                    Text (
                        "Henüz bir ödev yok",
                        textAlign = TextAlign.Center
                    )
                }
            }



        }


    }


}

@Composable
fun AnnouncementsSection(isExpanded: Boolean, announcements: List<StudentAnnouncementResponse>) {


    Card(
        modifier = Modifier.fillMaxWidth().height(350.dp)
            .background(Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = 5.dp
    ){
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Duyurular",
                fontWeight = FontWeight.Bold,
                fontFamily = customFontFamily,
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
            )
            Divider(color = Color.Black, thickness = 2.dp, modifier = Modifier.padding(horizontal = 10.dp))

            if (announcements.isNotEmpty()){
                announcements.forEach { announcement ->
                    AnnouncementsRow(announcement, isExpanded)
                }
            }else{
                Text (
                    text = "Gelen kutusu boş",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

        }
    }

}

@Composable
fun LabelWithSortIcon(label: String, fontSize: TextUnit) {
    Column(
        modifier = Modifier.height(45.dp).width(125.dp)
    ){
        Text(label, fontSize = fontSize, modifier = Modifier.alpha(0.7f), textAlign = TextAlign.Center , fontFamily = customFontFamily)
        Divider(thickness = 4.dp, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(5.dp))
    }

}

@Composable
fun SideDrawer(isExpended: Boolean, username: String, onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .animateContentSize()
            .fillMaxWidth(if (isExpended) 0.75f else 0.0f)
            .fillMaxHeight()
            .background(Color.White)
            .zIndex(1f)
    ) {
        if (isExpended) {
            Row(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.13f)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color.White, Color(0xFF5D5FEF)),
                            start = Offset(0f, 100f),
                            end = Offset(800f, 100f)
                        )
                    )
            ) {
                Column(modifier = Modifier.fillMaxHeight().fillMaxWidth(0.80f)) {
                    Text(username, modifier = Modifier.padding(top = 15.dp, start = 15.dp, bottom = 7.dp), fontFamily = customFontFamily)
                    Text("12-A", modifier = Modifier.padding(top = 7.dp, start = 15.dp, bottom = 15.dp), fontFamily = customFontFamily)
                }
                IconButton(modifier = Modifier.fillMaxSize(), onClick = onClose) {
                    Icon(painterResource(R.drawable.close_button), contentDescription = "Close Side Menu", modifier = Modifier.fillMaxSize().padding(15.dp))
                }
            }
            SideBarButton("Profil")
            GradientDivider()
            SideBarButton("Ana Sayfa")
            GradientDivider()
            SideBarButton("Geçmiş Sınavlar")
            GradientDivider()
            SideBarButton("Ödevler")
            GradientDivider()
            SideBarButton("Devamsızlık")
            GradientDivider()
            SideBarButton("Duyurular")
            GradientDivider()

            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = { println("clicked button") },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = buttonColors(backgroundColor = Color(0xFFD7D7D7))
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Text("Ayarlar", modifier = Modifier.fillMaxWidth(0.8f).align(Alignment.CenterVertically), fontFamily = customFontFamily)
                    Box(modifier = Modifier.fillMaxSize()) {
                        Icon(
                            painterResource(R.drawable.settings),
                            contentDescription = "Settings",
                            modifier = Modifier.align(Alignment.Center).size(20.dp)
                        )
                    }
                }
            }
            GradientDivider()
            Button(
                onClick = { println("clicked button") },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = buttonColors(backgroundColor = Color(0xFFD7D7D7))
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Text("Çıkış Yap", modifier = Modifier.fillMaxWidth(0.8f).align(Alignment.CenterVertically), fontFamily = customFontFamily)
                    Box(modifier = Modifier.fillMaxSize()) {
                        Icon(
                            painterResource(R.drawable.logout),
                            contentDescription = "Logout",
                            modifier = Modifier.align(Alignment.Center).size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
