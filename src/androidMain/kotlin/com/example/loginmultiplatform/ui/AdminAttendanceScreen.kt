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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.ui.focus.onFocusChanged
import com.example.loginmultiplatform.model.AttendanceResponse
import com.example.loginmultiplatform.model.AttendanceStats
import com.example.loginmultiplatform.model.StudentCourseResponse
import com.example.loginmultiplatform.model.TeacherAttendanceRequest
import com.example.loginmultiplatform.model.TeacherClassResponse
import com.example.loginmultiplatform.utils.CreateAttendancePDFforCoordinator
import com.example.loginmultiplatform.viewmodel.AttendanceViewModel
import com.example.loginmultiplatform.viewmodel.TeacherAttendanceViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
actual fun AdminAttendanceScreen(studentViewModel: AttendanceViewModel, teacherViewModel: TeacherAttendanceViewModel, navController: NavController) {
    val classes by teacherViewModel.teacherClasses.collectAsState()
    val courses by teacherViewModel.courseId.collectAsState() //List<TeacherCourseResponse>
    val allCourses by teacherViewModel.allCourse.collectAsState()
    val allClasses by teacherViewModel.allClass.collectAsState()
    val attendanceMap by studentViewModel.attendanceMap.collectAsState()
    val attendanceMapSingular by studentViewModel.attendanceMapSingular.collectAsState()
    val attendanceStatsMap by studentViewModel.attendanceStatsMap.collectAsState()
    val studentCoursesMap by studentViewModel.studentCoursesMap.collectAsState()

    var showStudentDetail by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var selectedCourseId by remember { mutableStateOf<Int?>(null) }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }


    val today = remember {
        val calendar = Calendar.getInstance()
        String.format("%d-%02d-%02d", calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR))
    }

    var selectedDate by remember { mutableStateOf( today ) }

    /*val isPastDate = remember(formatToReadableDateDatabase(selectedDate)) {
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

            val oneWeekAgo = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -7)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            selected != null && selected.before(todayWithoutTime) && !selected.before(oneWeekAgo)
        } catch (e: Exception) {
            false
        }
    }*/

    val studentComments = remember { mutableStateMapOf<Int, String>() }

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    //öğrenci yoklama durumları için map
    val attendanceOptions = listOf("Katıldı", "Katılmadı", "Geç Geldi")

    //seçili durumlar
    val attendanceStates = remember {
        mutableStateMapOf<Int, String>().apply {
            classes.flatMap { it.studentIdAndNames.entries }.forEach { (studentIdStr, _) ->
                val studentId = studentIdStr.toInt()
                this[studentId] = "Katıldı"
            }
        }
    }

    LaunchedEffect(Unit) {
        teacherViewModel.fetchTeacherClasses()
        teacherViewModel.fetchAllCourses()
        teacherViewModel.fetchAllClasses()
    }

    val filteredClasses by remember(selectedCourseId) {
        derivedStateOf {
            if (selectedCourseId != null) {
                val selectedCourse = allCourses.find { it.id == selectedCourseId }
                if (selectedCourse != null) {
                    allClasses.filter { classItem ->
                        selectedCourse.classEntityIds.contains(classItem.id)
                    }
                }
                else {
                    emptyList()
                }
            } else {
                emptyList()
            }
        }
    }

    LaunchedEffect(selectedDate, selectedCourseId, filteredClasses) {
        if (selectedCourseId != null) {
            filteredClasses.forEach { classItem ->
                classItem.studentIdAndNames.keys.forEach { studentId ->
                    try {
                        val sid = studentId.toInt()

                        studentViewModel.fetchAttendance(
                            studentId = sid,
                            startDate = formatToReadableDateDatabase(selectedDate),
                            endDate = formatToReadableDateDatabase(selectedDate)
                        )

                        studentViewModel.fetchAttendanceSingular(
                            studentId = sid,
                            startDate = startDate,
                            endDate = endDate
                        )

                        studentViewModel.fetchAttendanceStats(
                            studentId = sid,
                            classId = classItem.id
                        )

                        studentViewModel.fetchStudentCourses(sid)
                    } catch (_: Exception) {
                    }
                }
            }
        }
    }

    LaunchedEffect(attendanceMap, selectedDate) {
        attendanceStates.clear()
        studentComments.clear()

        attendanceMap.forEach { (studentId, attendanceList) ->
            val attendanceForDate = attendanceList.find {
                it.date == formatToReadableDateDatabase(selectedDate)
            }

            if (attendanceForDate != null) {
                attendanceStates[studentId] = mapAttendanceStatusforFrontend(attendanceForDate.status)
                studentComments[studentId] = attendanceForDate.comment ?: "Veri Bulunamadı"
            }
        }
    }

    LaunchedEffect(selectedDate, selectedCourseId, attendanceMap, attendanceStatsMap) {
        attendanceStates.clear()
        studentComments.clear()

        val selectedCourse = allCourses.find { it.id == selectedCourseId }
        if (selectedCourse != null) {
            val relevantClasses = allClasses.filter { classItem ->
                selectedCourse.classEntityIds.contains(classItem.id)
            }

            relevantClasses.forEach { classItem ->
                classItem.studentIdAndNames.keys.forEach { studentId ->
                    val sid = studentId.toInt()

                    val attendanceForDate = attendanceMap[sid]?.find {
                        it.date == formatToReadableDateDatabase(selectedDate) &&
                        it.classId.toInt() == classItem.id &&
                                it.courseId.toInt() == selectedCourseId
                    }

                    if (attendanceForDate != null) {
                        attendanceStates[sid] = mapAttendanceStatusforFrontend(attendanceForDate.status)
                        studentComments[sid] = attendanceForDate.comment ?: ""
                    }

                    val stats = attendanceStatsMap[sid to classItem.id]?.find {
                        it.classId == classItem.id && it.courseId == selectedCourseId
                    }

                    if (stats != null) {
                        Log.d("Stats", "Stats for $sid: $stats")
                    }
                }
            }
        }
    }



    // Sınıflar geldikten sonra teacherId kullanarak kursları getir
    LaunchedEffect(classes) {
        if (classes.isNotEmpty()) {
            val teacherId = classes[0].teacherId // İlk sınıftan teacherId alınır
            teacherViewModel.fetchTeacherCourses(teacherId)
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

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
        .verticalScroll(scrollState)
    ) {

        if (allCourses.isEmpty()) {
            Text("No courses available", modifier = Modifier.padding(16.dp))
        }

        LazyRow(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            items(allCourses) { course ->
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .clickable { selectedCourseId = course.id }
                        .background(
                            color = if (selectedCourseId == course.id) Color(0xFF334BBE) else Color(0xFFEDF2FE),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = course.name,
                        style = MaterialTheme.typography.body2,
                        color = if (selectedCourseId == course.id) Color.White else Color(0xFF334BBE),
                        fontFamily = customFontFamily,
                        fontWeight = FontWeight.Medium

                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))



        Spacer(modifier = Modifier.height(8.dp))

        if (selectedCourseId != null) {

            CustomDatePicker(
                selectedDate = selectedDate,
                onDateSelected = { date ->
                    selectedDate = date
                }
            )

            filteredClasses.forEach { classItem ->
                ExpendableClassCardsForAdmin(
                    classInfo = classItem,
                    attendanceStates = attendanceStates,
                    attendanceOptions = attendanceOptions,
                    onInfoClick = { studentId ->
                        showStudentDetail = classItem.id to studentId
                    },
                    classes = filteredClasses,
                    teacherViewModel = teacherViewModel,
                    selectedDate = selectedDate,
                    studentComments = studentComments,
                    context = context,
                    attendanceMap = attendanceMap,
                    attendanceMapSingular = attendanceMapSingular,
                    attendanceStatsMap = attendanceStatsMap,
                    studentCoursesMap = studentCoursesMap,
                    selectedCourseId = selectedCourseId!!,
                    allClasses = allClasses
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    if(showStudentDetail != null && selectedCourseId != null) {
        val (classId, studentId) = showStudentDetail!!

        val statsForThisStudentAndClass = attendanceStatsMap[studentId to classId].orEmpty()
            .filter { it.courseId == selectedCourseId }

        StudentAttendanceDetailDialogAdmin(
            onDismiss = { showStudentDetail = null },
            stats = statsForThisStudentAndClass,
        )
    }
}

@Composable
fun ExpendableClassCardsForAdmin(
    classInfo: TeacherClassResponse,
    attendanceStates: MutableMap<Int, String>,
    studentComments: MutableMap<Int, String>,
    attendanceOptions: List<String>,
    onInfoClick: (Int) -> Unit,

    classes: List<TeacherClassResponse>,
    teacherViewModel: TeacherAttendanceViewModel,
    selectedDate: String,
    allClasses: List<TeacherClassResponse>,

    attendanceMap: Map<Int, List<AttendanceResponse>>,
    attendanceMapSingular: Map<Int, List<AttendanceResponse>>,
    attendanceStatsMap: Map<Pair<Int, Int>, List<AttendanceStats>>,
    studentCoursesMap: Map<Int, List<StudentCourseResponse>>,

    context: Context,

    selectedCourseId: Int
) {

    var expanded by remember { mutableStateOf(false) }
    var expandedDropdown by remember { mutableStateOf(false) }
    val bulkOperationStatus by teacherViewModel.bulkOperationStatus.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showConfirmationDialog by remember { mutableStateOf(false) }
    var editingAttendanceId by remember { mutableStateOf<Int?>(null) }
    var originalStates = remember { mutableStateMapOf<Int, String>() }
    var originalComments = remember { mutableStateMapOf<Int, String>() }




    val today = remember {
        val calendar = Calendar.getInstance()
        String.format("%d-%02d-%02d", calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR))
    }

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

                        val attendanceForDate = filteredAttendanceData.find {
                            it.date == formatToReadableDateDatabase(selectedDate) &&
                                    it.classId.toInt() == classInfo.id &&
                                    it.courseId.toInt() == selectedCourseId &&
                                    it.studentId.toInt() == sid
                        }

                        var hasUserModified by remember { mutableStateOf(false) }
                        var textFieldValue by remember { mutableStateOf("") }

                        LaunchedEffect(key1 = attendanceForDate) {
                            if (attendanceForDate != null) {
                                // Bu map'ler mutlaka mutableStateMapOf olmalı!
                                attendanceStates[sid] = mapAttendanceStatusforFrontend(attendanceForDate.status)
                                studentComments[sid] = attendanceForDate.comment ?: "Yorum Yok"
                            }
                        }

                        LaunchedEffect(key1 = studentComments[sid]) {
                            // eğer sonradan veri dolduysa:
                            textFieldValue = studentComments[sid] ?: ""
                        }

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

                                DropdownMenu(
                                    expanded = expandedMenu,
                                    onDismissRequest = { expandedMenu = false }
                                ) {
                                    attendanceOptions.forEach { option ->
                                        DropdownMenuItem(onClick = {
                                            attendanceStates[sid] = option
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

                        Spacer(modifier = Modifier.height(2.dp))

                        OutlinedTextField(
                            value = textFieldValue,
                            onValueChange = { newText ->
                                textFieldValue = newText
                                hasUserModified = true
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
                                .defaultMinSize(minHeight = 56.dp)
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                            textFieldValue = ""
                                    }
                                },
                            singleLine = true,
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
                                focusedBorderColor = Color(0xFF334BBE),
                                disabledBorderColor = Color.Gray,
                                disabledTextColor = Color.Gray
                            )
                        )

                        LaunchedEffect(studentComments[sid]) {
                            if (!hasUserModified) {
                                textFieldValue = studentComments[sid] ?: ""
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
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

                            var showStartDatePicker by remember { mutableStateOf(false) }
                            var showEndDatePicker by remember { mutableStateOf(false) }
                            var startDateInput by remember { mutableStateOf("") }
                            var endDateInput by remember { mutableStateOf("") }
                            var selectedStudentName by remember { mutableStateOf("") }
                            var selectedStudentId by remember { mutableStateOf(-1) }

                            if (showStartDatePicker) {
                                val calendar = Calendar.getInstance()
                                DatePickerDialog(
                                    context,
                                    {_, year, month, dayOfMonth ->
                                        startDateInput = String.format("%d-%02d-%02d", year, month + 1, dayOfMonth)
                                        showStartDatePicker = false
                                        showEndDatePicker = true
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }

                            if (showEndDatePicker) {
                                val calendar = Calendar.getInstance()
                                DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        endDateInput = String.format(
                                            "%d-%02d-%02d",
                                            year,
                                            month + 1,
                                            dayOfMonth
                                        )
                                        showEndDatePicker = false

                                        if (selectedStudentId != -1) {
                                            generateReportForStudent(
                                                context = context,
                                                studentId = selectedStudentId,
                                                studentName = selectedStudentName,
                                                startDate = startDateInput,
                                                endDate = endDateInput,
                                                classInfo = classInfo,
                                                selectedCourseId = selectedCourseId,
                                                attendanceMapSingular = attendanceMapSingular,
                                                attendanceStatsMap = attendanceStatsMap,
                                                studentCoursesMap = studentCoursesMap,
                                                allClasses = allClasses
                                            )
                                        }
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }

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
                                            showStartDatePicker = true
                                            selectedStudentId = studentIdStr.toInt()
                                            selectedStudentName = studentName

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

                                        val editedAttendance = filteredAttendanceData.find { attendance ->
                                            val sid = attendance.studentId.toInt()
                                            val originalState = attendance.status
                                            val currentState = attendanceStates[sid]
                                            val originalComment = attendance.comment
                                            val currentComment = studentComments[sid]
                                            originalState != mapAttendanceStatus(currentState ?: "PRESENT") || originalComment != currentComment
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
                                        text = { Text("Geçmiş tarihe ait bir yoklama verisini değiştiriyorsunuz.\nYine de değişiklik yapmak istiyor musunuz?", fontFamily = customFontFamily, fontWeight = FontWeight.Normal, color = Color.Black) },
                                        confirmButton = {
                                            Button(onClick = {
                                                showConfirmationDialog = false
                                                editingAttendanceId?.let { attendanceId ->
                                                    val editedAttendance = filteredAttendanceData.find { it.attendanceId.toInt() == attendanceId }
                                                    if (editedAttendance != null) {
                                                        val requestBody = mapOf(
                                                            "studentId" to editedAttendance.studentId.toInt(),
                                                            "date" to editedAttendance.date,
                                                            "status" to (mapAttendanceStatus(attendanceStates[editedAttendance.studentId.toInt()]
                                                                ?: "PRESENT")),
                                                            "comment" to (studentComments[editedAttendance.studentId.toInt()]
                                                                ?: ""),
                                                            "classId" to editedAttendance.classId.toInt(),
                                                            "courseId" to editedAttendance.classId.toInt()
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
    }
    CustomSnackbar(snackbarHostState = snackbarHostState)
}

@Composable
fun StudentAttendanceDetailDialogAdmin(
    onDismiss: () -> Unit,
    stats: List<AttendanceStats>,
) {

    // Öğrenci detaylarını gösteren dialog
    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            Column {
                Text("Yoklama İstatistikleri", fontWeight = FontWeight.Bold, fontFamily = customFontFamily, style = MaterialTheme.typography.subtitle1, color = Color(0xFF334BBE))
                Divider(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))

                if(stats.isEmpty()) {
                    Text(
                        "Bu öğrenciye ait yoklama istatistiği bulunamadı.",
                        fontFamily = customFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                } else {

                    stats.forEach { stat ->
                        Text(
                            "Toplam Ders: ${stat.totalClasses}",
                            fontFamily = customFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                        Text(
                            "Ortalama Devam: %${stat.attendancePercentage}",
                            fontFamily = customFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                        Text(
                            "Katıldığı Dersler: ${stat.presentCount}",
                            fontFamily = customFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                        Text(
                            "Gelmediği Dersler: ${stat.absentCount}",
                            fontFamily = customFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                        Text(
                            "Geç Kaldığı Dersler: ${stat.lateCount}",
                            fontFamily = customFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Son Yoklama Verisi",
                            fontFamily = customFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334BBE),
                            style = MaterialTheme.typography.subtitle1
                        )
                        Divider(modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))

                        if (stat.recentAttendance.isNullOrEmpty()) {
                            Text(
                                "Son yoklama verisi bulunamadı.",
                                fontFamily = customFontFamily,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                        } else {
                            Column {
                                stat.recentAttendance.forEach { recentAttendance ->
                                    var date =
                                        formatToReadableDateTeacherSecond(recentAttendance.date)
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

                                        "EXCUSED" -> Text(
                                            "Durum: Geç Geldi",
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

                        Spacer(modifier = Modifier.height(4.dp))
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






