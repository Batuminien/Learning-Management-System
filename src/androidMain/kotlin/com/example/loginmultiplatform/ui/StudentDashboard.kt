package com.example.loginmultiplatform.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import com.example.loginmultiplatform.ui.components.BottomNavigationBar
import com.example.loginmultiplatform.ui.components.TopBar
import androidx.navigation.NavController
import com.example.loginmultiplatform.viewmodel.AttendanceViewModel
import com.example.loginmultiplatform.viewmodel.LoginViewModel
import com.example.loginmultiplatform.viewmodel.StudentAnnouncementViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
actual fun StudentDashboard(navController: NavController, loginViewModel: LoginViewModel, attendanceViewModel: AttendanceViewModel) {
    val pagerState = rememberPagerState(initialPage = 1, pageCount = { 4 })
    val studentId by loginViewModel.studentId.collectAsState()
    val username by loginViewModel.username.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val studentAnnouncementViewModel = StudentAnnouncementViewModel()

    Scaffold(
        topBar = { TopBar(userName = username, onSettingsClick = { }, onProfileClick = {}) },
        bottomBar = { BottomNavigationBar(pagerState = pagerState) }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            userScrollEnabled = true
        ) { page ->
            when (page) {
                0 -> AttendanceScreen(attendanceViewModel, navController, studentId = studentId ?: -1, classId = 1)
                1 -> DashboardPage(username)
                2 -> HomeworkPage("HOMEWORK PAGE")
                3 -> StudentAnnouncementPage(loginViewModel, studentAnnouncementViewModel, navController)
            }
        }

        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.currentPage to pagerState.currentPageOffsetFraction }
                .collect{ (currentPage, offsetFraction) ->
                    coroutineScope.launch {
                        when (currentPage) {
                            0 -> {
                                if (offsetFraction > 0.5) {
                                    pagerState.animateScrollToPage(3)
                                }
                            }
                        }
                    }
                }
        }
    }
}

@Composable
fun DashboardPage(username: String?) {
    var isExpended by remember { mutableStateOf(false) }

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
                    modifier = Modifier.fillMaxSize().padding(top = 80.dp, bottom = 80.dp),
                    contentPadding = PaddingValues(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { WeeklyScheduleSection() }
                    item { ExamsSection() }
                    item { HomeworkSection() }
                    item { AnnouncementsSection(isExpended) }
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
fun HomeworkRow(course: Homeworks) {
    Row(modifier = Modifier.height(50.dp).fillMaxWidth()) {
        Row(modifier = Modifier.width(95.dp).height(50.dp)) {
            Spacer(modifier = Modifier.width(7.dp))
            Text(
                text = course.CourseName,
                fontSize = 12.sp,
                lineHeight = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxSize(),
                fontFamily = customFontFamily
            )
        }
        Row(modifier = Modifier.width(105.dp).height(50.dp)) {
            Text(
                text = course.CourseConcept,
                fontSize = 12.sp,
                lineHeight = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxSize(),
                fontFamily = customFontFamily
            )
        }
        Row(modifier = Modifier.width(45.dp).height(22.dp), verticalAlignment = Alignment.Bottom) {
            Text(
                text = course.FinishDate,
                fontSize = 12.sp,
                lineHeight = 12.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth(),
                fontFamily = customFontFamily
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .width(55.dp)
                .height(25.dp)
                .background(
                    if (course.Statue) Color.Green else Color.Red,
                    RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = if (course.Statue) "Aktif" else "Bitti", fontSize = 10.sp, fontFamily = customFontFamily)
        }
    }
}

@Composable
fun AnnouncementsRow(item: Announcements, isExpanded: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = 3.dp,
        backgroundColor = if (!item.state) Color(0xFFD8EBF5) else Color(0xFFF2F2F2)
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
                Text(text = item.description, lineHeight = 15.sp, fontSize = 10.sp, fontFamily = customFontFamily)
            }
            Box(modifier = Modifier.fillMaxSize()) {
                Icon(
                    painter = painterResource(if (item.state) R.drawable.read else R.drawable.un_read),
                    contentDescription = if (item.state) "read Mail icon" else "un_read Mail icon",
                    modifier = Modifier.size(25.dp).align(Alignment.Center),
                    tint = Color.Unspecified
                )
            }
        }
    }
}

@Composable
fun Bars(personal: Float, overall: Float, examName: String, examType: Boolean) {
    val divisor = if (examType) 120f else 80f
    val personalRatio = personal / divisor
    val overallRatio = overall / divisor

    Column(modifier = Modifier.fillMaxHeight().width(55.dp)) {
        Row(modifier = Modifier.width(55.dp).height(175.dp), verticalAlignment = Alignment.Bottom) {
            Box(
                modifier = Modifier
                    .fillMaxHeight(personalRatio)
                    .width(25.dp)
                    .background(Color(0xFF4AB58E), RoundedCornerShape(3.dp))
            )
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxHeight(overallRatio)
                    .width(25.dp)
                    .background(Color(0xFFFFCF00), RoundedCornerShape(4.dp))
            )
        }

        Row(modifier = Modifier.height(20.dp).fillMaxWidth()) {
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
        }

        Text(
            text = examName,
            fontSize = 10.sp,
            color = Color.Black,
            textAlign = TextAlign.Center,
            lineHeight = 13.sp,
            fontFamily = customFontFamily
        )
    }
}

//========================= Sections as Composables =========================//
@Composable
fun WeeklyScheduleSection() {
    val days = listOf("Pzt", "Salı", "Çrş", "Prş", "Cuma", "Cmt", "Pzr")
    val timeslots = (10..18)

    Column(
        modifier = Modifier.fillMaxWidth().height(400.dp)
            .background(Color.White, RoundedCornerShape(20.dp))
    ) {
        Spacer(modifier = Modifier.height(5.dp))
        Text("HAFTALIK DERS PROGRAMI", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontFamily = customFontFamily)
        Spacer(modifier = Modifier.height(5.dp))

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

                LazyRow(modifier = Modifier.fillMaxSize()) {
                    days.forEach { day ->
                        item {
                            Box(modifier = Modifier.fillMaxHeight().width(100.dp)) {
                                DummyLessons().forEach { (start_hour, end_hour, Lday, name) ->
                                    if (day == Lday) {
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
                                                .background(get_lesson_color(name).copy(alpha = 0.8f), RoundedCornerShape(10.dp))
                                        ) {
                                            Text(
                                                text = name,
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
                                    Text(day, fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontFamily = customFontFamily)
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

@Composable
fun ExamsSection() {
    val exams = DummyExams()
    var personalAverage = 0f
    var overallAverage = 0f

    exams.forEach { (personel, overall, _, _) ->
        personalAverage += personel
        overallAverage += overall
    }
    personalAverage /= exams.size
    overallAverage /= exams.size

    Column(
        modifier = Modifier.fillMaxWidth().height(425.dp)
            .padding(10.dp)
            .background(Color.White, RoundedCornerShape(20.dp))
    ) {
        Spacer(modifier = Modifier.height(30.dp))
        Text("Geçmiş Sınavlar", fontWeight = FontWeight.Bold, fontFamily = customFontFamily, modifier = Modifier
            .fillMaxWidth()
            .height(25.dp)
            .padding(start = 15.dp))
        LazyRow(
            modifier = Modifier.height(240.dp).fillMaxWidth()
                .background(Color(0xFFacc5e9), RoundedCornerShape(20.dp)),
            verticalAlignment = Alignment.Bottom
        ) {
            item { Spacer(modifier = Modifier.width(20.dp)) }
            exams.forEach { (personel, overall, examName, examType) ->
                item {
                    Bars(personel, overall, examName, examType)
                    Spacer(modifier = Modifier.width(20.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(25.dp))
        Row(
            modifier = Modifier.fillMaxWidth().height(30.dp).padding(start = 25.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(30.dp)
                    .background(color = Color(0xFFE2FFF3), RoundedCornerShape(5.dp))
            ) {
                Icon(
                    painterResource(R.drawable.bag),
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.Center).size(18.dp),
                    tint = Color(0xFF4AB58E)
                )
            }
            Spacer(modifier = Modifier.width(15.dp))
            Text("Kişisel Ortalama", fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFamily = customFontFamily)
            Spacer(modifier = Modifier.width(40.dp))
            Text(formatFloat(personalAverage, 2), color = Color(0xFF27AE60), fontSize = 13.sp, fontFamily = customFontFamily)
        }

        Spacer(modifier = Modifier.height(5.dp))
        Row(
            modifier = Modifier.fillMaxWidth().height(30.dp).padding(start = 25.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(30.dp)
                    .background(color = Color(0xFFFFF4DE), RoundedCornerShape(5.dp))
            ) {
                Icon(
                    painterResource(R.drawable.ticket_star),
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.Center).size(18.dp),
                    tint = Color(0xFFFFA800)
                )
            }
            Spacer(modifier = Modifier.width(15.dp))
            Text("Genel Ortalama", fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFamily = customFontFamily)
            Spacer(modifier = Modifier.width(46.dp))
            Text(formatFloat(overallAverage, 2), color = Color(0xFFFFA412), fontSize = 13.sp, fontFamily = customFontFamily)
        }
    }
}

@Composable
fun HomeworkSection() {
    val homeworks = getHomeworkList(DummyHomeworks())
    val myFontSize = 11.sp
    Column(
        modifier = Modifier.fillMaxWidth().height(335.dp)
            .padding(10.dp)
            .background(Color.White, RoundedCornerShape(20.dp))
    ) {
        Text("Ödevler", fontWeight = FontWeight.Bold, fontFamily = customFontFamily, modifier = Modifier
            .height(50.dp)
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 25.dp))

        Row(modifier = Modifier.fillMaxWidth().height(25.dp)) {
            Spacer(modifier = Modifier.width(20.dp))
            LabelWithSortIcon("Ders adı", myFontSize)
            Spacer(modifier = Modifier.width(40.dp))
            LabelWithSortIcon("Konu", myFontSize)
            Spacer(modifier = Modifier.width(40.dp))
            LabelWithSortIcon("Bitiş", myFontSize)
            Spacer(modifier = Modifier.width(10.dp))
            LabelWithSortIcon("Durum", myFontSize)
        }

        Spacer(modifier = Modifier.height(10.dp))
        homeworks.forEach {
            HomeworkRow(it)
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun AnnouncementsSection(isExpanded: Boolean) {
    val announcements = getAnnouncementList(DummyAnnouncements())
    Column(
        modifier = Modifier.fillMaxWidth().height(350.dp)
            .padding(10.dp)
            .background(Color.White, RoundedCornerShape(20.dp))
    ) {
        Text(
            text = "Duyurular",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.height(50.dp)
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 25.dp),
            fontFamily = customFontFamily
        )
        Divider(color = Color.Black, thickness = 2.dp, modifier = Modifier.padding(horizontal = 10.dp))
        announcements.forEach {
            AnnouncementsRow(it, isExpanded)
        }
    }
}

@Composable
fun LabelWithSortIcon(label: String, fontSize: TextUnit) {
    Text(label, fontSize = fontSize, modifier = Modifier.alpha(0.7f), fontFamily = customFontFamily)
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.down_arrow),
            contentDescription = "Sort Icon",
            modifier = Modifier
                .size(15.dp)
                .alpha(0.7f)
        )
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
