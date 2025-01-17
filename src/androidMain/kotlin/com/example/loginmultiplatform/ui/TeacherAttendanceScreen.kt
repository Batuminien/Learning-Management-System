package com.example.loginmultiplatform.ui

import android.app.DatePickerDialog
import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.loginmultiplatform.R
import com.example.loginmultiplatform.model.AttendanceResponse
import com.example.loginmultiplatform.model.AttendanceStats
import com.example.loginmultiplatform.model.CourseStatisticsResponse
import com.example.loginmultiplatform.model.StudentCourseResponse
import com.example.loginmultiplatform.model.TeacherAttendanceRequest
import com.example.loginmultiplatform.model.TeacherClassResponse
import com.example.loginmultiplatform.model.TeacherCourseResponse
import com.example.loginmultiplatform.utils.CreateAttendancePDFforTeacher
import com.example.loginmultiplatform.viewmodel.AttendanceViewModel
import com.example.loginmultiplatform.viewmodel.TeacherAttendanceViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

val timesNewRoman = FontFamily(
    Font(R.font.times, FontWeight.Normal)
)

fun formatToReadableDateTeacher(dateString: String): String {
    val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    val outputFormat = SimpleDateFormat("d MMMM yyyy\nEEEE", Locale("tr"))
    val date = inputFormat.parse(dateString)
    return date?.let { outputFormat.format(it) } ?: dateString
}

fun formatToReadableDateTeacherSecond(dateString: String): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val date = inputFormat.parse(dateString)

    val outputFormat = SimpleDateFormat("d MMMM yyyy EEEE", Locale("tr"))
    return outputFormat.format(date)
}

fun formatToReadableDateDatabase(dateString: String): String {
    val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    val date = inputFormat.parse(dateString)

    val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale("tr"))
    return outputFormat.format(date)
}

fun mapAttendanceStatus(status: String): String {
    return when (status) {
        "Katıldı" -> "PRESENT"
        "Katılmadı" -> "ABSENT"
        "Geç Geldi" -> "LATE"
        "Mazeretli" -> "EXCUSED"
        else -> "PRESENT"
    }
}

fun mapAttendanceStatusforFrontend(status: String): String {
    return when (status) {
        "PRESENT" -> "Katıldı"
        "ABSENT" -> "Katılmadı"
        "LATE" -> "Geç Geldi"
        "EXCUSED" -> "Mazeretli"
        else -> "Veri Yok"
    }
}

@Composable
actual fun TeacherAttendanceScreen(studentViewModel: AttendanceViewModel ,teacherViewModel: TeacherAttendanceViewModel, navController: NavController, teacherId: Int?, courseId: Int?) {

    Log.e("courseıd", "courseId: ${courseId}")
    val classes by teacherViewModel.teacherClasses.collectAsState()
    val courses by teacherViewModel.courseId.collectAsState() //List<TeacherCourseResponse>
    val stats by teacherViewModel.courseStats.collectAsState() //List<CourseStatisticsResponse>

    val attendanceMap by studentViewModel.attendanceMap.collectAsState()
    val attendanceStatsMap by studentViewModel.attendanceStatsMap.collectAsState()
    val studentCoursesMap by studentViewModel.studentCoursesMap.collectAsState()
    //val teacherCourses by studentViewModel.teacherCourses.collectAsState()


    var showStudentId by remember { mutableStateOf<Int?>(null) }


    val today = remember {
        val calendar = Calendar.getInstance()
        String.format("%d-%02d-%02d", calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR))
    }

    var selectedDate by remember { mutableStateOf( today ) }

    val isPastDate = remember(formatToReadableDateDatabase(selectedDate)) {
        try {
            val selected = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(
                formatToReadableDateDatabase(selectedDate)
            )
            val todayWithoutTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            selected?.before(todayWithoutTime) == true
        } catch (e: Exception) {
            false
        }
    }

    val studentComments = remember { mutableStateMapOf<Int, String>() }
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val startDate = "2022-01-01"
    val endDate = "2026-12-31"

    //öğrenci yoklama durumları için map
    val attendanceOptions = listOf("Katıldı", "Katılmadı", "Geç Geldi", "Mazeretli")

    val attendanceStates = remember { mutableStateMapOf<Int, String>() }

    LaunchedEffect(Unit) {
        teacherViewModel.fetchTeacherClasses()
        teacherId?.let { studentViewModel.fetchTeacherCourses(it) }
    }

    LaunchedEffect(selectedDate, classes) {
        attendanceStates.clear()
        studentComments.clear()
        classes.forEach { classItem ->
            classItem.studentIdAndNames.keys.forEach { studentId ->
                try {
                    studentViewModel.fetchAttendance(
                        studentId = studentId.toInt(),
                        startDate = startDate,
                        endDate = endDate
                    )

                    studentViewModel.fetchAttendanceStats(studentId.toInt(), classItem.id)
                    studentViewModel.fetchStudentCourses(studentId.toInt())
                } catch(e: Exception) {
                    //Log.e("TeacherAttendanceScreen", "Error fetching attendance: ${e.message}")
                }
            }
        }
    }

    LaunchedEffect(attendanceMap, selectedDate) {
        attendanceMap.forEach { (studentId, attendanceList) ->
            val filteredAttendance = attendanceList.find {
                it.date == formatToReadableDateDatabase(selectedDate)
            }
            filteredAttendance?.let { attendance ->
                attendanceStates[studentId] = mapAttendanceStatusforFrontend(attendance.status)
                studentComments[studentId] = attendance.comment ?: ""
            }
        }
    }



    // Sınıflar geldikten sonra teacherId kullanarak kursları getir
    LaunchedEffect(classes) {
        if (classes.isNotEmpty()) {
            val teacherIdFromClasses = classes[0].teacherCourses[0].teacherId // İlk sınıftan teacherId alınır
            teacherViewModel.fetchTeacherCourses(teacherIdFromClasses)
        }
    }

    LaunchedEffect(classes, courses) {
        if(classes.isNotEmpty() && courses.isNotEmpty()) {
            classes.forEach { classItem ->

                val relatedCourses = courses.filter {
                    it.classEntityIds.contains(classItem.id)
                }

                relatedCourses.forEach { course ->
                    teacherViewModel.fetchCourseStatistics(
                        courseId = course.id,
                        classId = classItem.id,
                        startDate = startDate,
                        endDate = endDate
                    )
                }
            }
        }
    }

    //val teacherCourseId = teacherCourses.firstOrNull()?.teacherCourses?.firstOrNull()?.courseId

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
        .verticalScroll(scrollState))
    {

        Spacer(modifier = Modifier.height(16.dp))

        CustomDatePicker(
            selectedDate = selectedDate,
            onDateSelected = { date ->
                selectedDate = date
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        classes.forEach { classItem ->
            ExpendableClassCard(
                classInfo = classItem,
                attendanceStates = attendanceStates,
                attendanceOptions = attendanceOptions,
                onInfoClick = { studentId ->
                    showStudentId = studentId
                },
                classes = classes,
                courses = courses,
                teacherViewModel = teacherViewModel,
                selectedDate = selectedDate,
                studentComments = studentComments,
                isPastDate = isPastDate,
                context = context,
                startDate = startDate,
                endDate = endDate,
                attendanceMap = attendanceMap,
                attendanceStatsMap = attendanceStatsMap,
                studentCoursesMap = studentCoursesMap,
                selectedCourseId = courses.firstOrNull()?.id ?: 0,
                today = today
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }

    if(showStudentId != null) {

            StudentAttendanceDetailDialog(
                studentId = showStudentId!!,
                onDismiss = { showStudentId = null },
                stats = stats
                //teacherCourseId = teacherCourseId,
            )
    }
}

@Composable
fun ExpendableClassCard(
    classInfo: TeacherClassResponse,
    attendanceStates: MutableMap<Int, String>,
    studentComments: MutableMap<Int, String>,
    isPastDate: Boolean,
    attendanceOptions: List<String>,
    onInfoClick: (Int) -> Unit,

    classes: List<TeacherClassResponse>,
    courses: List<TeacherCourseResponse>,
    teacherViewModel: TeacherAttendanceViewModel,
    selectedDate: String,

    attendanceMap: Map<Int, List<AttendanceResponse>>,
    attendanceStatsMap: Map<Pair<Int, Int>, List<AttendanceStats>>,
    studentCoursesMap: Map<Int, List<StudentCourseResponse>>,

    context: Context,
    startDate: String,
    endDate: String,

    selectedCourseId: Int,
    today: String
) {

    var expanded by remember { mutableStateOf(false) }
    var expandedDropdown by remember { mutableStateOf(false) }
    val bulkOperationStatus by teacherViewModel.bulkOperationStatus.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showConfirmationDialog by remember { mutableStateOf(false) }
    var editingAttendanceId by remember { mutableStateOf<Int?>(null) }
    var originalStates = remember { mutableStateMapOf<Int, String>() }
    var originalComments = remember { mutableStateMapOf<Int, String>() }

    val hasUserModified = remember { mutableStateMapOf<Int, Boolean>() }

    val filteredAttendanceData = attendanceMap
        .filter { (studentId, _) ->
            classInfo.studentIdAndNames.keys.contains(studentId.toString())
        }
        .flatMap { (_, attendanceList) ->
            attendanceList.filter {
                it.classId.toInt() == classInfo.id && it.courseId.toInt() == selectedCourseId
            }
        }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = 4.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    classInfo.name,
                    style = MaterialTheme.typography.body1,
                    fontWeight = FontWeight.Bold,
                    fontFamily = customFontFamily,
                    color = Color.Black
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }

            if (expanded) {
                Divider()

                Column(modifier = Modifier.padding(16.dp)) {
                    classInfo.studentIdAndNames.forEach { (idStr, student) ->
                        val sid = idStr.toInt()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { onInfoClick(sid)}) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(color = Color(0xFF334BBE), shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("i", color = Color.White, fontWeight = FontWeight.Bold, fontFamily = timesNewRoman)
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(student, modifier = Modifier.weight(1f), fontFamily = customFontFamily, fontWeight = FontWeight.Medium)

                            var expandedMenu by remember { mutableStateOf(false) }
                            Box {
                                Text(
                                    attendanceStates[sid] ?: "Katıldı",
                                    modifier = Modifier
                                        .clickable { expandedMenu = true }
                                        .border(1.dp, Color(0xFF334BBE), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontFamily = customFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                                if(!isPastDate) {
                                    DropdownMenu(
                                        expanded = expandedMenu,
                                        onDismissRequest = { expandedMenu = false }
                                    ) {
                                        attendanceOptions.forEach { option ->
                                            DropdownMenuItem(onClick = {
                                                attendanceStates[sid] = option
                                                hasUserModified[sid] = true
                                                expandedMenu = false
                                            }) {
                                                Text(
                                                    option,
                                                    fontFamily = customFontFamily,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color.Black
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        OutlinedTextField(
                            value = studentComments[sid] ?: "",
                            onValueChange = { newText ->
                                studentComments[sid] = newText
                            },
                            textStyle = LocalTextStyle.current.copy(color = Color.Black, fontSize = 14.sp),
                            label = {
                                Text(
                                    text = "Açıklama",
                                    fontFamily = customFontFamily,
                                    color = Color(0xFF334BBE),
                                    fontSize = 12.sp
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 56.dp),
                            singleLine = true,
                            enabled = !isPastDate,
                            maxLines = 1,
                            trailingIcon = {
                                Icon(Icons.Rounded.Edit, contentDescription = "Edit Icon", tint = Color(0xFF334BBE), modifier = Modifier.size(16.dp))
                            },
                            visualTransformation = VisualTransformation.None,
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions.Default,
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                unfocusedBorderColor = Color.Black,
                                cursorColor = Color(0xFF334BBE),
                                focusedBorderColor = Color(0xFF334BBE)
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Download,
                            contentDescription = "Rapor İndir",
                            tint = Color(0xFF334BBE),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        Box {
                            Text(
                                "Rapor İndir",
                                modifier = Modifier
                                    .clickable { expandedDropdown = true }
                                    .padding(8.dp),
                                fontFamily = customFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF334BBE)
                            )

                            DropdownMenu(
                                expanded = expandedDropdown,
                                onDismissRequest = { expandedDropdown = false }
                            ) {


                                classInfo.studentIdAndNames.forEach { (studentIdStr, studentName) ->

                                    DropdownMenuItem(onClick = {
                                        expandedDropdown = false
                                        val sid = studentIdStr.toInt()

                                        // İlgili sınıfa ait kursu bul
                                        val relatedCourse = courses.firstOrNull { it.classEntityIds.contains(classInfo.id) }
                                        val teacherCourseId = relatedCourse?.id ?: 0

                                        val allAttendanceForStudent = attendanceMap[sid] ?: emptyList()

                                        val filteredAttendanceData = allAttendanceForStudent.filter {
                                            it.courseId.toInt() == teacherCourseId && it.classId.toInt() == classInfo.id
                                        }

                                        val allStatsForThisClass = attendanceStatsMap[sid to classInfo.id] ?: emptyList()


                                        val filteredStats = allStatsForThisClass.filter {
                                            it.courseId == teacherCourseId
                                        }

                                        val studentCourseList = studentCoursesMap[sid] ?: emptyList()

                                        CreateAttendancePDFforTeacher(
                                            context = context,
                                            groupedData = mapOf(classInfo.id.toLong() to filteredAttendanceData),
                                            startDate = startDate,
                                            endDate = endDate,
                                            statistics = filteredStats,
                                            courses = studentCourseList,
                                            classId = classInfo.id,
                                            courseId = teacherCourseId
                                        )

                                    }) {
                                        Text(
                                            text = studentName,
                                            fontFamily = customFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                        }
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {

                            Button(
                                onClick = {
                                    originalStates.clear()
                                    originalStates.putAll(attendanceStates)

                                    originalComments.clear()
                                    originalComments.putAll(studentComments)

                                    if (!isPastDate) {
                                        val editedAttendance = filteredAttendanceData.find { attendance ->
                                            if (attendance.date == formatToReadableDateDatabase(selectedDate)) {
                                                val sid = attendance.studentId.toInt()
                                                val originalState = attendance.status
                                                val currentState = attendanceStates[sid]
                                                val originalComment = attendance.comment
                                                val currentComment = studentComments[sid]

                                                originalState != mapAttendanceStatus(currentState ?: "PRESENT") || originalComment != currentComment
                                            } else {
                                                false
                                            }
                                        }

                                        if (editedAttendance != null) {
                                            editingAttendanceId = editedAttendance.attendanceId.toInt()
                                            showConfirmationDialog = true
                                        } else {
                                            val bulkList = classes.flatMap { classItem ->
                                                classItem.studentIdAndNames.map { (idStr, _) ->
                                                    val sid = idStr.toInt()

                                                    TeacherAttendanceRequest(
                                                        studentId = sid,
                                                        date = formatToReadableDateDatabase(selectedDate),
                                                        status = mapAttendanceStatus(attendanceStates[sid] ?: "PRESENT"),
                                                        comment = studentComments[sid] ?: "",
                                                        classId = classItem.id,
                                                        courseId = courses.firstOrNull()?.id ?: 0
                                                    )
                                                }
                                            }
                                            teacherViewModel.saveAttendanceBulk(bulkList)
                                        }
                                    } else {
                                        val bulkList = classes.flatMap { classItem ->
                                            classItem.studentIdAndNames.map { (idStr, _) ->
                                                val sid = idStr.toInt()
                                                TeacherAttendanceRequest(
                                                    studentId = sid,
                                                    date = formatToReadableDateDatabase(selectedDate),
                                                    status = mapAttendanceStatus(
                                                        attendanceStates[sid] ?: "PRESENT"
                                                    ),
                                                    comment = studentComments[sid] ?: "",
                                                    classId = classItem.id,
                                                    courseId = selectedCourseId
                                                )
                                            }
                                        }
                                        teacherViewModel.saveAttendanceBulk(bulkList)
                                    }
                                },
                                modifier = Modifier
                                    .width(110.dp)
                                    .padding(end = 16.dp)
                                    .padding(bottom = 8.dp),
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF334BBE)),
                                enabled = !isPastDate || selectedDate == today
                            ) {
                                Text(
                                    "Kaydet",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = customFontFamily
                                )
                            }

                            LaunchedEffect(bulkOperationStatus) {
                                bulkOperationStatus?.let { message ->
                                    snackbarHostState.showSnackbar(message)
                                }
                            }

                            if (showConfirmationDialog) {
                                AlertDialog(
                                    onDismissRequest = { showConfirmationDialog = false },
                                    title = { Text("UYARI", fontFamily = customFontFamily, fontWeight = FontWeight.Bold, color = Color.Red) },
                                    text = { Text("Var olan bir yoklama verisini değiştiriyorsunuz.\nYine de değişiklik yapmak istiyor musunuz?", fontFamily = customFontFamily, fontWeight = FontWeight.Normal, color = Color.Black) },
                                    confirmButton = {
                                        Button(onClick = {
                                            showConfirmationDialog = false
                                            editingAttendanceId?.let { attendanceId ->
                                                val editedAttendance = filteredAttendanceData.find {
                                                    it.attendanceId.toInt() == attendanceId
                                                }
                                                if (editedAttendance != null) {
                                                    val sid = editedAttendance.studentId.toInt()
                                                    val requestBody = mapOf(
                                                        "studentId" to sid,
                                                        "date" to editedAttendance.date,
                                                        "status" to mapAttendanceStatus(attendanceStates[sid] ?: "PRESENT"),
                                                        "comment" to (studentComments[sid] ?: ""),
                                                        "classId" to editedAttendance.classId.toInt(),
                                                        "courseId" to editedAttendance.courseId.toInt()
                                                    )

                                                    teacherViewModel.updateAttendance(
                                                        attendanceId = attendanceId,
                                                        studentId = requestBody["studentId"] as Int,
                                                        date = requestBody["date"] as String,
                                                        status = requestBody["status"] as String,
                                                        comment = requestBody["comment"] as String,
                                                        classId = requestBody["classId"] as Int,
                                                        courseId = requestBody["courseId"] as Int
                                                    )
                                                }
                                            }
                                        },
                                            colors = ButtonDefaults.buttonColors(
                                                backgroundColor = Color(0xFF334BBE),
                                                contentColor = Color.White
                                            )
                                        ) {
                                            Text("Evet", fontFamily = customFontFamily, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                                        }
                                    },
                                    dismissButton = {
                                        Button(onClick = {
                                            showConfirmationDialog = false
                                            attendanceStates.clear()
                                            attendanceStates.putAll(originalStates)
                                            studentComments.clear()
                                            studentComments.putAll(originalComments)
                                        },
                                            colors = ButtonDefaults.buttonColors(
                                                backgroundColor = Color.Gray,
                                                contentColor = Color.White
                                            )) {
                                            Text("Hayır", fontFamily = customFontFamily, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                                        }
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
    CustomSnackbar(snackbarHostState = snackbarHostState)
}

@Composable
fun StudentAttendanceDetailDialog(
    studentId: Int,
    onDismiss: () -> Unit,
    stats: List<CourseStatisticsResponse>
    ) {

    // Öğrenci detaylarını gösteren dialog
    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            Column {
                Text("Yoklama İstatistikleri", fontWeight = FontWeight.Bold, fontFamily = customFontFamily, style = MaterialTheme.typography.subtitle1, color = Color(0xFF334BBE))
                Divider(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))

                stats.forEach { stat ->
                    if(stat.studentId == studentId) {
                        Text(
                            "Toplam Ders Sayısı: ${stat.totalClasses}",
                            fontFamily = customFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                        Text(
                            "Ortalama Devam Oranı: %${stat.attendancePercentage}",
                            fontFamily = customFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                        Text(
                            "Katıldığı Ders Sayısı: ${stat.presentCount}",
                            fontFamily = customFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                        Text(
                            "Gelmediği Ders Sayısı: ${stat.absentCount}",
                            fontFamily = customFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                        Text(
                            "Geç Kaldığı Ders Sayısı: ${stat.lateCount}",
                            fontFamily = customFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                        Text(
                            "Mazeretli Ders Sayısı: ${stat.excusedCount}",
                            fontFamily = customFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(
                    "Son Yoklama Verisi",
                    fontFamily = customFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334BBE),
                    style = MaterialTheme.typography.subtitle1
                )
                Divider(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))

                if(stats.all { it.recentAttendance.isNullOrEmpty() }) {
                    Text(
                        "Son yoklama verisi bulunamadı.",
                        fontFamily = customFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                } else {
                    Column {
                        stats.forEach { stat ->
                            stat.recentAttendance.forEach { recentAttendance ->
                                var date = formatToReadableDateTeacherSecond(recentAttendance.date)
                                Text(
                                    "Tarih: ${date}",
                                    fontFamily = customFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                                when (recentAttendance.status) {
                                    "ABSENT" -> Text(
                                        "Durum: Katılmadı",
                                        fontFamily = customFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Black
                                    )

                                    "LATE" -> Text(
                                        "Durum: Geç Geldi",
                                        fontFamily = customFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Black
                                    )

                                    "EXCUSED" -> Text(
                                        "Durum: Mazeretli",
                                        fontFamily = customFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Black
                                    )
                                }
                                Text(
                                    "Açıklama: ${recentAttendance.comment ?: "-"}",
                                    fontFamily = customFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat", fontFamily = customFontFamily, fontWeight = FontWeight.Bold, color = Color(0xFF334BBE))
            }
        }
    )
}

@Composable
fun CustomDatePicker(
    selectedDate: String,
    onDateSelected: (String) -> Unit
) {


    var showDatePicker by remember { mutableStateOf(false) }

    if(showDatePicker) {
        val context = LocalContext.current
        val calendar = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val date = String.format("%d-%02d-%02d", dayOfMonth, month + 1, year)
                onDateSelected(date)
                showDatePicker = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.setOnDismissListener {
            showDatePicker = false // Tarih seçmeden kapatıldığında sıfırla
        }

        datePickerDialog.show()
    }

    Box(
        modifier = Modifier
            .border(2.dp, color = Color(0xFF334BBE), shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clickable { showDatePicker = true }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.CalendarToday,
                tint = Color(0xFF334BBE),
                contentDescription = "Tarih Seç",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = formatToReadableDateTeacher(selectedDate),
                style = MaterialTheme.typography.body2.copy(
                    color = Color(0xFF334BBE),
                    fontWeight = FontWeight.Bold,
                    fontFamily = customFontFamily
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}