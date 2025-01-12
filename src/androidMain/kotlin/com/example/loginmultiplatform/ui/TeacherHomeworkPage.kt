package com.example.loginmultiplatform.ui

//noinspection UsingMaterialAndMaterial3Libraries
import android.content.ContentResolver
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.LocalPlatformContext
import com.example.loginmultiplatform.getPlatformResourceContainer
import com.example.loginmultiplatform.model.TeacherAssignmentResponse
import com.example.loginmultiplatform.model.AssignmentDocument
import com.example.loginmultiplatform.model.TeacherAssignmentRequest
import com.example.loginmultiplatform.viewmodel.TeacherAssignmentViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.mutableStateMapOf
import com.example.loginmultiplatform.model.BulkGradeItem
import com.example.loginmultiplatform.model.BulkGradeRequest
import com.example.loginmultiplatform.model.GradeDTO
import com.example.loginmultiplatform.model.StudentSubmission
import com.example.loginmultiplatform.ui.components.SharedDocument
import com.example.loginmultiplatform.ui.components.rememberDocumentManager
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


@RequiresApi(Build.VERSION_CODES.O)
fun getCurrentDateTime(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .withZone(ZoneOffset.UTC)
    return formatter.format(Instant.now())
}

@RequiresApi(Build.VERSION_CODES.O)
fun getCurrentDate(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        .withZone(ZoneOffset.UTC)
    return formatter.format(Instant.now())
}

@Composable
fun get_buttons(content_of_assignment: MutableState<Int>, content_value: Int , buttonText : String, classexpanded : MutableState<Boolean>, courseExpanded: MutableState<Boolean>){

    IconButton (
        modifier = Modifier.height(60.dp).width(105.dp).
        background( color = if (content_of_assignment.value == content_value)  Color(0xFF5270FF) else Color.White , RoundedCornerShape(10.dp) ),
        onClick = {
            content_of_assignment.value = content_value; print("Clicked\n")
            classexpanded.value = false
            courseExpanded.value = false

        }

    ){
        Row (
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ){

            Icon(
                painter = painterResource(id = if (content_of_assignment.value == content_value ) getPlatformResourceContainer().eyeOpen else getPlatformResourceContainer().eyeClose ),
                contentDescription = buttonText,
                modifier = Modifier.size(40.dp).padding(7.dp),
                tint = Color(0xFF334BBE)
            )
            Text(
                text = buttonText,
                fontSize = 13.sp,
                modifier = Modifier.align(Alignment.CenterVertically),
                color = if (content_of_assignment.value == content_value) Color.White else Color.Black
            )
        }
    }
}

fun saveFileFromUri(context: Context, uri: Uri, outputFileName: String): File? {
    val contentResolver: ContentResolver = context.contentResolver
    val outputDir = context.filesDir // Internal storage directory
    val outputFile = File(outputDir, outputFileName)

    try {
        contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(outputFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }

    return outputFile
}

fun findStudentSubmissionById(studentId : Int, studentSubmission: List<StudentSubmission>?) : StudentSubmission?{
    studentSubmission?.forEach { submission ->
        if (studentId == submission.studentId){
            return submission
        }
    }

    return null
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayHomeworks(assignment: TeacherAssignmentResponse, userName: String, teacherViewModel: TeacherAssignmentViewModel, teacherId: Int?, changeStatus : MutableState<Boolean>, mode : MutableState<Boolean>){

    var displayExpand by remember { mutableStateOf(false) }
    var dateDisplayExpanded by remember { mutableStateOf(false) }
    var checkedDisplayDate by remember { mutableStateOf(false) }

    val context = LocalPlatformContext.current

    var selectedDateLastDisplay by remember { mutableStateOf(assignment.dueDate) }

    var titleDisplay by remember { mutableStateOf(assignment.title) }

    val focusRequesterTitleDisplay = remember { FocusRequester() }

    var isFocusedTitleDisplay by remember { mutableStateOf(false) }

    var descriptionDisplay by remember { mutableStateOf(assignment.description) }

    val focusRequesterDescDisplay = remember { FocusRequester() }

    var isFocusedDescDisplay by remember { mutableStateOf(false) }

    var selectedFile by remember { mutableStateOf<AssignmentDocument?>(assignment.teacherDocuments) }

    titleDisplay = assignment.title
    descriptionDisplay = assignment.description

    // Callback to handle the selected document
    val onDocumentSelectedUpdate: (SharedDocument?) -> Unit = { document ->
        if (document != null) {

            try {

            }catch (e: Exception){
                println(e.toString())
            }


            Toast.makeText(context, "Selected file: ${document.getFileName()}, fileSize: ${document.fileSize()}, fileTime: ${getCurrentDateTime()}", Toast.LENGTH_SHORT).show()

        }
    }

    val documentManagerUpdate = rememberDocumentManager(onResult = onDocumentSelectedUpdate)


    Card(
        modifier = Modifier.fillMaxWidth().height( if (displayExpand)  800.dp else 100.dp).padding(10.dp),
        elevation = 8.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column (
            modifier = Modifier.fillMaxSize()
        ) {

            Row(
                modifier = Modifier.fillMaxWidth().height( 80.dp).
                clickable{
                    displayExpand = !displayExpand
                },
                verticalAlignment = Alignment.CenterVertically
            ){

                Icon(
                    imageVector = Icons.Default.Assignment,
                    contentDescription = "Assignment",
                    modifier = Modifier.fillMaxWidth(0.2f).size(50.dp).padding(10.dp)
                )

                Column (
                    modifier = Modifier.fillMaxHeight().fillMaxWidth(0.55f)
                ){
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = assignment.title,
                        modifier = Modifier.fillMaxWidth().fillMaxHeight(0.5f),
                        fontSize = 13.sp
                    )





                    Text(
                        text = assignment.courseName,
                        modifier = Modifier.fillMaxSize(),
                        fontSize = 13.sp
                    )
                }

                Column (
                    modifier = Modifier.fillMaxHeight().fillMaxWidth(0.6f)
                ){
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = assignment.createdDate,
                        modifier = Modifier.fillMaxWidth().fillMaxHeight(0.5f),
                        fontSize = 13.sp
                    )

                    Text(
                        text = assignment.dueDate,
                        modifier = Modifier.fillMaxSize(),
                        fontSize = 13.sp
                    )
                }

                Icon(
                    imageVector = if (displayExpand) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().padding(10.dp)
                )
            }
            if (displayExpand) {

                Divider(modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(20.dp))

                Text (
                    text = "   Sınıf Adı",
                    modifier = Modifier.fillMaxWidth().height(20.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                        .padding(12.dp)

                ) {
                    Text(text = assignment.className, color = Color.Black)
                }





                Spacer(modifier = Modifier.height(20.dp))

                Text (
                    text = "   Ders Adı",
                    modifier = Modifier.fillMaxWidth().height(20.dp)
                )

                Box(
                    modifier = Modifier

                        .fillMaxWidth()
                        .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                        .padding(12.dp)

                ) {
                    Text(text = assignment.courseName, color = Color.Black)
                }





                Spacer(modifier =Modifier.height(20.dp))


                Text (
                    text = "   Başlık",
                    modifier = Modifier.fillMaxWidth().height(20.dp)
                )

                Box (
                    modifier = Modifier.fillMaxWidth().height(50.dp).background(Color.LightGray, RoundedCornerShape(8.dp))
                ){
                    BasicTextField(
                        value = titleDisplay,
                        onValueChange = { titleDisplay = it },
                        modifier = Modifier.fillMaxSize().offset(x = 15.dp, y = 15.dp)
                            .focusRequester(focusRequesterTitleDisplay).onFocusChanged { focusState: FocusState ->
                                isFocusedTitleDisplay = focusState.isFocused
                                if (isFocusedTitleDisplay && titleDisplay == assignment.title) {
                                    titleDisplay = "" // Clear placeholder text when focused
                                }
                            }
                    )
                }


                Spacer(modifier = Modifier.height(20.dp))

                Text (
                    text = "   Açıklama",
                    modifier = Modifier.fillMaxWidth().height(20.dp)
                )

                Box (
                    modifier = Modifier.fillMaxWidth().height(100.dp).background(Color.LightGray, RoundedCornerShape(8.dp))
                ){
                    descriptionDisplay?.let {
                        BasicTextField(
                            value = it,
                            onValueChange = { descriptionDisplay = it },
                            modifier = Modifier.fillMaxSize().offset(x = 15.dp, y = 15.dp)
                                .focusRequester(focusRequesterDescDisplay).onFocusChanged { focusState: FocusState ->
                                    isFocusedDescDisplay = focusState.isFocused
                                    if (isFocusedDescDisplay && descriptionDisplay == assignment.description) {
                                        descriptionDisplay = "" // Clear placeholder text when focused
                                    }
                                }
                        )
                    }
                }


                Spacer(modifier = Modifier.height(20.dp))


                Text (
                    text = "   Bitiş Tarihi",
                    modifier = Modifier.fillMaxWidth().height(20.dp)
                )


                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { dateDisplayExpanded = !dateDisplayExpanded }
                        .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                        .padding(12.dp)

                ) {
                    Text(text = if (checkedDisplayDate) selectedDateLastDisplay else assignment.dueDate, color = Color.Black)
                }



                if (dateDisplayExpanded){
                    val datePickerState = rememberDatePickerState()
                    DatePickerDialog(
                        onDismissRequest = { /*TODO*/  },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    val selectedDate = Calendar.getInstance().apply {
                                        timeInMillis = datePickerState.selectedDateMillis!!
                                    }
                                    if (selectedDate.after(Calendar.getInstance())) {
                                        Toast.makeText(
                                            context,
                                            "Seçilen tarih ${dateFormatter.format(selectedDate.time)} kaydedildi",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        selectedDateLastDisplay = dateFormatter.format(selectedDate.time)
                                        dateDisplayExpanded = false
                                        checkedDisplayDate = true
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Seçilen tarih geçmemiş bir tarih olmalı lütfen başka bir tarih seçiniz",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            ) { Text("Tamam") }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    dateDisplayExpanded= false
                                }
                            ) { Text("İptal") }
                        }
                    )
                    {
                        DatePicker(state = datePickerState)
                    }

                }



                Spacer(modifier = Modifier.height(20.dp))

                Text (
                    text = "Doküman",
                    modifier = Modifier.fillMaxWidth().height(20.dp)
                )

                Box (
                    modifier = Modifier.fillMaxWidth().height(60.dp)
                        .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ){
                    Row (
                        modifier = Modifier.fillMaxSize()
                    ){
                        Button(
                            modifier = Modifier.fillMaxHeight().fillMaxWidth(if (selectedFile == null)0.5f else 0.8f),

                            onClick = {
                                if (selectedFile == null){

                                    documentManagerUpdate.launch()

                                }



                            },

                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.White
                            )



                        ){
                            Text(
                                text = selectedFile?.fileName ?: "Dosya Seç",
                                modifier = Modifier.fillMaxSize(),
                                textAlign = TextAlign.Center
                            )
                        }

                        if (selectedFile == null){
                            Spacer(modifier = Modifier.width(10.dp))
                            Text (
                                text = "Dosya Seçilmedi",
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                        }else {
                            IconButton(
                                onClick = {
                                    selectedFile = null

                                },
                                modifier = Modifier.fillMaxSize()
                            ){
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete file"
                                )
                            }
                        }




                    }
                }


                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    Spacer(modifier = Modifier.width(15.dp))

                    Button(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        onClick = {
                            displayExpand = !displayExpand
                            changeStatus.value = !changeStatus.value
                            teacherId?.let {
                                TeacherAssignmentRequest(
                                    it,
                                    titleDisplay,
                                    descriptionDisplay,
                                    selectedDateLastDisplay,
                                    assignment.classId,
                                    assignment.courseId,
                                    null
                                )
                            }?.let {
                                mode.value = true
                                teacherViewModel.updateHomework(assignment.id,
                                    it
                                )
                            }

                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFF334BBE )
                        )

                    ){
                        Text(
                            text = "Güncelle",
                            color = Color.White
                        )
                    }

                    Button(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        onClick = {
                            displayExpand = !displayExpand
                            changeStatus.value = !changeStatus.value
                            if (assignment.teacherDocuments != null){
                                teacherViewModel.deleteDocument(assignment.teacherDocuments.documentId)
                            }

                            teacherViewModel.deleteAssignment(assignment.id)
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.Red
                        )

                    ){
                        Text(
                            text = "Sil",
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(15.dp))
                }

            }
        }

    }




}



@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayPastHomeworks(assignment: TeacherAssignmentResponse, userName: String, teacherViewModel: TeacherAssignmentViewModel, teacherId: Int?, changeStatus: MutableState<Boolean>, mode: MutableState<Boolean>, classStudents: Map<String, String>?, focusRequester: FocusRequester.Companion){

    var displayExpand by remember { mutableStateOf(false) }
    var dateDisplayExpanded by remember { mutableStateOf(false) }
    var checkedDisplayDate by remember { mutableStateOf(false) }

    val context = LocalPlatformContext.current

    var selectedDateLastDisplay by remember { mutableStateOf(assignment.dueDate) }

    var grades = remember { mutableStateMapOf<Int, String>() }

    var feedbacks = remember { mutableStateMapOf<Int, String?>() }

    val focusRequesterTitleDisplay = remember { FocusRequester() }

    var isFocusedGrade by remember { mutableStateOf(false) }

    var feedback by remember { mutableStateOf("Bir geri dönüş giriniz") }

    val focusRequesterDescDisplay = remember { FocusRequester() }

    var isFocusedFeedback by remember { mutableStateOf(false) }

    var selectedFile by remember { mutableStateOf<AssignmentDocument?>(assignment.teacherDocuments) }

    var grade by remember { mutableStateOf("0-100") }



    // Callback to handle the selected document
    val onDocumentSelectedUpdate: (SharedDocument?) -> Unit = { document ->
        if (document != null) {

            try {

            }catch (e: Exception){
                println(e.toString())
            }


            Toast.makeText(context, "Selected file: ${document.getFileName()}, fileSize: ${document.fileSize()}, fileTime: ${getCurrentDateTime()}", Toast.LENGTH_SHORT).show()

        }
    }

    val documentManagerUpdate = rememberDocumentManager(onResult = onDocumentSelectedUpdate)


    Card(
        modifier = Modifier.fillMaxWidth().height( if (displayExpand)  500.dp else 100.dp).padding(10.dp),
        elevation = 8.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column (
            modifier = Modifier.fillMaxSize()
        ) {

            Row(
                modifier = Modifier.fillMaxWidth().height( 80.dp).
                clickable{
                    displayExpand = !displayExpand
                },
                verticalAlignment = Alignment.CenterVertically
            ){

                Icon(
                    imageVector = Icons.Default.Assignment,
                    contentDescription = "Assignment",
                    modifier = Modifier.fillMaxWidth(0.2f).size(50.dp).padding(10.dp)
                )

                Column (
                    modifier = Modifier.fillMaxHeight().fillMaxWidth(0.55f)
                ){
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = assignment.title,
                        modifier = Modifier.fillMaxWidth().fillMaxHeight(0.5f),
                        fontSize = 13.sp
                    )





                    Text(
                        text = assignment.courseName,
                        modifier = Modifier.fillMaxSize(),
                        fontSize = 13.sp
                    )
                }

                Column (
                    modifier = Modifier.fillMaxHeight().fillMaxWidth(0.6f)
                ){
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = assignment.createdDate,
                        modifier = Modifier.fillMaxWidth().fillMaxHeight(0.5f),
                        fontSize = 13.sp
                    )

                    Text(
                        text = assignment.dueDate,
                        modifier = Modifier.fillMaxSize(),
                        fontSize = 13.sp
                    )
                }

                Icon(
                    imageVector = if (displayExpand) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().padding(10.dp)
                )
            }
            if (displayExpand) {

                classStudents?.keys?.forEach{students ->
                    val submission = findStudentSubmissionById(students.toInt(), assignment.studentSubmissions)
                    if (submission != null && submission.status == "GRADED") {
                        grades[students.toInt()] =  submission.grade.toString()
                        feedbacks[students.toInt()] =  submission.feedback ?: "-"

                    }else {
                        feedbacks[students.toInt()] = "Bir geri dönüş giriniz"
                        grades[students.toInt()] = "0-100"
                    }

                    /*if (feedbacks[students.toInt()] == null){
                        feedbacks[students.toInt()] = "Bir geri dönüş giriniz"
                    }*/


                }

                Spacer(modifier = Modifier.height(15.dp))
                LazyColumn (
                    modifier = Modifier.fillMaxWidth().height(325.dp).background(Color.LightGray)
                ){
                    classStudents?.keys?.forEach { students ->
                        item {
                            val submission = findStudentSubmissionById(students.toInt(), assignment.studentSubmissions)

                            Card(
                                modifier = Modifier.height(375.dp).fillParentMaxWidth().padding(12.dp).background(Color.Gray)
                            ){
                                Column(
                                    modifier = Modifier.fillMaxSize().background(Color.Transparent),
                                    verticalArrangement = Arrangement.SpaceAround
                                ){


                                    Row (
                                        modifier = Modifier.fillMaxWidth().background(Color.Transparent),
                                        horizontalArrangement = Arrangement.SpaceAround

                                    ){


                                        Column{
                                            Text(
                                                text = "Öğrenci adı"
                                            )

                                            Spacer(modifier = Modifier.height(5.dp))

                                            classStudents[students]?.let {
                                                Text(
                                                    text = it
                                                )
                                            }


                                        }




                                        Column{
                                            Text(
                                                text = "Ödev Durumu"
                                            )

                                            Spacer(modifier = Modifier.height(5.dp))

                                            Text(
                                                text = if(submission != null ) if(submission.status == "GRADED") "Puanlandı"
                                                else if (submission.status == "SUBMITTED") "Teslim edildi"
                                                else (if (getCurrentDate() > assignment.dueDate) "Bekleniyor" else "Teslim edilmedi")
                                                else "Ödev yüklenmedi"
                                            )
                                        }

                                    }


                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.Center
                                    ){
                                        Text (
                                            text = "Yüklenen Döküman",
                                            modifier = Modifier.fillParentMaxWidth(),
                                            textAlign = TextAlign.Center

                                        )

                                        Spacer(modifier = Modifier.height(5.dp))


                                        Text (
                                            text = if (submission != null ) if (submission.document == null) "Döküman eklenmedi" else submission.document.fileName else "-",
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillParentMaxWidth()

                                        )

                                    }


                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.Center
                                    ){
                                        Text (
                                            text = "Teslim Notu",
                                            modifier = Modifier.fillParentMaxWidth(),
                                            textAlign = TextAlign.Center

                                        )

                                        Spacer(modifier = Modifier.height(5.dp))


                                        Text (
                                            text = if (submission != null ) submission.comment ?: "-" else "-",
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillParentMaxWidth()

                                        )

                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ){
                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = "Ödev Notu :"
                                        )

                                        Spacer(modifier = Modifier.width(15.dp))


                                        Box (
                                            modifier = Modifier.fillMaxWidth().height(50.dp).padding(start = 10.dp, end = 10.dp).background(Color.LightGray, RoundedCornerShape(8.dp))
                                        ){

                                            if (submission != null){
                                                if (submission.status == "GRADED"){
                                                    grade = submission.grade.toString()
                                                }
                                            }else {
                                                grade = "Ödev teslim edilmedi"
                                            }

                                            BasicTextField(
                                                value = grades[students.toInt()]!!,
                                                onValueChange = { if (it.length <= 3 ) grades[students.toInt()] = it },
                                                enabled = (submission != null),
                                                modifier = Modifier.fillMaxSize().offset(x = 15.dp, y = 15.dp)
                                                    .focusRequester(focusRequester.Default).onFocusChanged { focusState: FocusState ->
                                                        isFocusedGrade = focusState.isFocused
                                                        if (isFocusedGrade && grade == "0-100") {
                                                            grades[students.toInt()] = "" // Clear placeholder text when focused
                                                        }

                                                        if (!isFocusedGrade && grades[students.toInt()] != "0-100" && grades[students.toInt()] != "" && submission != null){
                                                            if (grades[students.toInt()]!!.toLongOrNull() == null || grades[students.toInt()]!!.toLong() > 100  || grades[students.toInt()]!!.toLong() < 0 || (grades[students.toInt()]!!.toLong() < 100 && grades[students.toInt()]!!.length > 2)){
                                                                Toast.makeText(
                                                                    context,
                                                                    "Lütfen 0 ile 100 arası bir not giriniz",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                                grades[students.toInt()] = "0-100"
                                                            }
                                                        }
                                                    }
                                            )


                                        }

                                    }

                                    Spacer(modifier = Modifier.height(20.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ){
                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = "Geri Dönüt :"
                                        )

                                        Spacer(modifier = Modifier.width(15.dp))


                                        Box (
                                            modifier = Modifier.fillMaxWidth().height(130.dp).padding(start = 10.dp, end = 10.dp).background(Color.LightGray, RoundedCornerShape(8.dp))
                                        ){
                                            if (submission == null || submission.status == "PENDING"){
                                                feedbacks[students.toInt()] = "Ödev teslim edilmedi"
                                            }


                                            BasicTextField(
                                                value = feedbacks[students.toInt()]!!,
                                                onValueChange = { if (it.length <= 150) feedbacks[students.toInt()] = it },
                                                enabled = (submission != null),
                                                modifier = Modifier.fillMaxSize().padding(end = 20.dp, bottom = 20.dp).offset(x = 15.dp, y = 15.dp)
                                                    .focusRequester(focusRequester.Default).onFocusChanged { focusState: FocusState ->
                                                        isFocusedFeedback = focusState.isFocused
                                                        if (isFocusedFeedback && feedbacks[students.toInt()] == "Bir geri dönüş giriniz") {
                                                            feedbacks[students.toInt()] = "" // Clear placeholder text when focused
                                                        }


                                                    }
                                            )

                                        }

                                    }


                                    Spacer(modifier = Modifier.height(8.dp))


                                }
                            }



                        }
                    }
                }

                Button(
                    onClick = {
                        //TODO
                        val bulkList = mutableListOf<BulkGradeItem>()
                        isFocusedFeedback = false

                        if (classStudents != null){
                            if (grades.keys.size != classStudents.keys.size){
                                Toast.makeText(
                                    context,
                                    "Eksik veya hatalı notlandırmalar var",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }else{
                                classStudents.keys.forEach { studentsIds ->
                                    bulkList.add(BulkGradeItem(studentsIds.toInt(), GradeDTO(grades[studentsIds.toInt()]!!.toLong(), if(feedbacks[studentsIds.toInt()] == "-" || feedbacks[studentsIds.toInt()] == "" || feedbacks[studentsIds.toInt()] == "Bir geri dönüş giriniz") null else feedbacks[studentsIds.toInt()])))

                                }
                                println("Cevaplanacak ödevler : $bulkList")
                                teacherViewModel.bulkGrades(assignment.id, BulkGradeRequest(bulkList))
                                changeStatus.value = !changeStatus.value
                                displayExpand = false
                            }
                        }




                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF334BBE)
                    )


                ){
                    Text(
                        text = "Tamamla",
                        color = Color.White
                    )
                }

            }
        }

    }




}





@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun TeacherHomeworkPage (title: String, teacherViewModel : TeacherAssignmentViewModel, teacherId :Int?, username: String) {

    val classes by teacherViewModel.teacherClasses.collectAsState()

    val courses by teacherViewModel.courseId.collectAsState()

    val coursesSearch by teacherViewModel.courseSearchId.collectAsState()

    val coursesPast by teacherViewModel.coursePastId.collectAsState()

    val errorMessage by teacherViewModel.errorMessage.collectAsState()

    val bulkOperationStatus by teacherViewModel.bulkOperationStatus.collectAsState()

    val teacherAssignments by teacherViewModel.teacherAssignments.collectAsState()

    val teacherAssignmentsPast by teacherViewModel.teacherAssignmentsPast.collectAsState()

    val classStudents by teacherViewModel.classStudents.collectAsState()

    val addedDoc by teacherViewModel.returnedDoc.collectAsState()

    val errorDoc by teacherViewModel.errorDoc.collectAsState()

    val docSuccess by teacherViewModel.docSended.collectAsState()

    val sendedAssignment by teacherViewModel.sendedAssignment.collectAsState()

    val isLoad by teacherViewModel.isLoading.collectAsState()

    val isDeleted by teacherViewModel.isDeleted.collectAsState()

    val isUpdated by teacherViewModel.isUpdated.collectAsState()


    LaunchedEffect(Unit) {
        teacherViewModel.fetchTeacherClasses()

    }




    var content_of_assignment = remember { mutableStateOf(1) }

    var classexpanded = remember { mutableStateOf(false) }

    var selectedClass by remember { mutableStateOf("Sınıf Seçiniz") }

    var selectedClassId by remember { mutableStateOf( 0 ) }

    var courseexpanded = remember { mutableStateOf(false) }

    var selectedCourse by remember { mutableStateOf<String?>("Ders Seçiniz") }

    var selectedCourseId by remember { mutableStateOf(0) }

    var selectedDateLast by remember { mutableStateOf("gg.aa.yyyy") }

    var searchHomework by remember { mutableStateOf(false) }

    val context = LocalPlatformContext.current

    LaunchedEffect(selectedClass){
        try{
            if (selectedClass != "Sınıf Seçiniz"){
                teacherId?.let { teacherViewModel.fetchTeacherCourses(it) }
                Toast.makeText(
                    context,
                    "Dersler yükleniyor",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }catch ( e : Exception){
            Toast.makeText(
                context,
                e.toString(),
                Toast.LENGTH_SHORT
            ).show()
        }



    }







    var checkedDate by remember { mutableStateOf(false)}


    val date = remember {
        Calendar.getInstance()
    }


    var dateExpanded by remember { mutableStateOf(false) }


    val focusRequester = remember { FocusRequester }

    val focusRequesterTitle = remember { FocusRequester() }
    val focusManagerTitle: FocusManager = LocalFocusManager.current
    var title by remember { mutableStateOf("Başlık giriniz") }
    var isFocusedTitle by remember { mutableStateOf(false) }


    val focusRequesterDesc = remember { FocusRequester() }
    val focusManagerDesc: FocusManager = LocalFocusManager.current
    var description by remember { mutableStateOf("Açıklama giriniz") }
    var isFocusedDesc by remember { mutableStateOf(false) }

    var assignmentDocument by remember { mutableStateOf<AssignmentDocument?>(null) }

    val scope = rememberCoroutineScope()


    var selectedFileContent by remember { mutableStateOf<SharedDocument?>(null) }
    var fileName by remember { mutableStateOf<String>("") }
    var filePath by remember { mutableStateOf<String>("") }
    var fileSize by remember { mutableStateOf<Long>(0) }
    val uploadTime by remember { mutableStateOf(getCurrentDateTime()) }



    // Callback to handle the selected document
    val onDocumentSelected: (SharedDocument?) -> Unit = { document ->
        if (document != null) {
            selectedFileContent = document
            fileName = document.getFileName().toString()

            // Display detailed file info in Toast
            Toast.makeText(
                context,
                "Selected file: ${document.getFileName()}, fileSize: ${document.fileSize()}, fileTime: ${getCurrentDateTime()}",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            // Handle case where no file is selected
            Toast.makeText(context, "No document selected.", Toast.LENGTH_SHORT).show()
        }
    }

    val documentManager = rememberDocumentManager(onResult = onDocumentSelected)



    var searchExpand  by remember { mutableStateOf(false) }

    var selectedClassSearch by remember { mutableStateOf("Sınıf Seçiniz") }

    var selectedClassSearchId by remember { mutableStateOf( -1 ) }

    var selectedCourseSearch by remember { mutableStateOf<String?>("Ders Seçiniz") }

    var selectedCourseSearchId by remember { mutableStateOf(-1) }

    var selectedDateLastSearch by remember { mutableStateOf("gg.aa.yyyy") }


    var pastExpand  by remember { mutableStateOf(false) }

    var selectedClassPast by remember { mutableStateOf("Sınıf Seçiniz") }

    var selectedClassPastId by remember { mutableStateOf( -1 ) }

    var selectedCoursePast by remember { mutableStateOf<String?>("Ders Seçiniz") }

    var selectedCoursePastId by remember { mutableStateOf(-1) }

    var selectedDateLastPast by remember { mutableStateOf("gg.aa.yyyy") }


    LaunchedEffect(selectedClassSearch){
        try{
            teacherId?.let { teacherViewModel.fetchTeacherCoursesSearch(it) }


        }catch (e : Exception){
            Toast.makeText(
                context,
                e.toString(),
                Toast.LENGTH_SHORT
            ).show()

        }

    }

    var homeworkSearched by remember { mutableStateOf(false) }
    var refreshLazyColumn by remember { mutableStateOf(false) }



    var mode =  remember { mutableStateOf(true) }
    LaunchedEffect(searchHomework){
        if (teacherId != null ) {
            println("Exploooosion")
            println(selectedCourseSearchId)
            println(selectedClassSearchId)
            println(selectedDateLastSearch)
            if (  (selectedClassSearchId != -1 && selectedCourseSearchId != -1) || (selectedClassPastId != -1 && selectedCoursePastId != -1) ) {

                if ( if (mode.value) selectedDateLastSearch == "gg.aa.yyyy" else selectedDateLastPast == "gg.aa.yyyy"){
                    if (mode.value){
                        teacherViewModel.fetchTeacherAssignments(
                            teacherId,
                            selectedClassSearchId,
                            selectedCourseSearchId,
                            "",
                            mode.value
                        )
                    }else {
                        teacherViewModel.fetchTeacherAssignments(
                            teacherId,
                            selectedCoursePastId,
                            selectedCoursePastId,
                            "",
                            mode.value
                        )
                    }

                }else{
                    if (mode.value){
                        teacherViewModel.fetchTeacherAssignments(
                            teacherId,
                            selectedClassSearchId ,
                            selectedCourseSearchId ,
                            selectedDateLastSearch,
                            mode.value
                        )
                    }else {
                        teacherViewModel.fetchTeacherAssignments(
                            teacherId,
                            selectedClassPastId,
                            selectedCoursePastId,
                            selectedDateLastPast,
                            mode.value
                        )
                    }

                }
                homeworkSearched = true
                refreshLazyColumn = false

            }
        }
    }

    LaunchedEffect(teacherAssignments){
        if (homeworkSearched){



            Toast.makeText(
                context,
                "Ödevler başarı ile yüklendi. Ödev sayısı ${teacherAssignments.size}",
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    LaunchedEffect(selectedClassPast){
        teacherId?.let { teacherViewModel.fetchTeacherCoursesPast(it) }
    }


    var changeStatus = remember { mutableStateOf(false) }
    LaunchedEffect(changeStatus.value){

        searchHomework = !searchHomework
        changeStatus.value = false
        println("Bir şeyler oldu")
    }

    LaunchedEffect(sendedAssignment) {


        if (selectedFileContent != null && sendedAssignment != null){


            teacherViewModel.addDocument(assignmentId =  sendedAssignment!!.id, shareddocument = selectedFileContent!!, context = context)

        }else {
            Toast.makeText(
                context,
                "Ödev atarken bir sorun oluştu",
                Toast.LENGTH_SHORT
            ).show()
        }
    }



    var docAdded by remember { mutableStateOf(false) }

    LaunchedEffect(docSuccess){
        if (docSuccess){
            println("Dosya başarı ile eklendi")
        }else {
            println("Dosya eklenemedi")
            sendedAssignment?.let { teacherViewModel.deleteAssignment(it.id) }
        }
    }

    LaunchedEffect(errorDoc){
        if (sendedAssignment != null)
            teacherViewModel.deleteAssignment(sendedAssignment!!.id)

    }


    LaunchedEffect(classStudents){
        description = classStudents?.entries.toString()
    }

Box(
modifier = Modifier
    .fillMaxSize().
    clickable(
        onClick = {
            classexpanded.value = false
            courseexpanded.value = false
            focusManagerTitle.clearFocus()
            focusManagerDesc.clearFocus()
        },
        interactionSource = remember { MutableInteractionSource() },
        indication = null
    )

) {
Card (
    modifier = Modifier.
        fillMaxWidth().
        height(80.dp).
        padding(4.dp),

    elevation = 6.dp,
    backgroundColor = Color.White,
    shape = RoundedCornerShape(8.dp)
) {
    Row (
        modifier = Modifier.
        fillMaxWidth().
        height(80.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ){

        get_buttons(content_of_assignment,0 , "Ödev Ekle", classexpanded, courseexpanded)

        get_buttons(content_of_assignment,1 , "Aktif Ödevler", classexpanded, courseexpanded)

        get_buttons(content_of_assignment,2 ,"Geçmiş Ödevler", classexpanded, courseexpanded)

    }
}

if (content_of_assignment.value == 0){
    Card (
        modifier = Modifier.offset(y = 85.dp).fillMaxSize().padding(4.dp).padding(bottom = 88.dp),
        backgroundColor = Color.White,
        shape = RoundedCornerShape(8.dp),
        elevation = 6.dp
    ){
        LazyColumn (
            modifier = Modifier.fillMaxSize()
        ) {
            item {

                Box (
                    modifier = Modifier.fillMaxWidth().height(800.dp)
                ) {

                    Row(
                        modifier = Modifier.offset(y = 700.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ){
                        Button(

                            onClick = {
                                //TODO
                                if (title != "Başlık giriniz"  && selectedClass != "Sınıf Seçiniz" && selectedCourse != "Ders Seçiniz" && selectedDateLast != "gg.aa.yyyy"){
                                    var newAssignment = teacherId?.let {
                                        TeacherAssignmentRequest(
                                            teacherId =  it,
                                            title =  title,
                                            description = description,
                                            dueDate =  selectedDateLast,
                                            classId =  selectedClassId,
                                            courseId =  selectedCourseId,
                                            document = null)
                                    }

                                    if (newAssignment != null) {
                                        docAdded = true

                                        teacherViewModel.addAssignment(newAssignment)

                                    }


                                }else {
                                    Toast.makeText(
                                        context,
                                        "Eksik bilgi girdiniz",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                title = "Başlık giriniz"
                                selectedClass = "Sınıf Seçiniz"
                                selectedCourse = "Ders Seçiniz"
                                selectedDateLast = "gg.aa.yyyy"
                                description = "Açıklama giriniz"
                                checkedDate = false



                            },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFF334BBE )
                            )

                        ){
                            Text(
                                text = "Oluştur",
                                color = Color.White
                            )
                        }
                    }





                    Box (
                        modifier = Modifier.offset(y = 600.dp).fillMaxWidth().height(60.dp)
                            .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ){
                        Row (
                            modifier = Modifier.fillMaxSize()
                        ){
                            Button(
                                modifier = Modifier.fillMaxHeight().fillMaxWidth(if (selectedFileContent == null)0.5f else 0.8f),

                                onClick = {
                                    if (selectedFileContent == null){

                                        documentManager.launch()

                                    }



                                },

                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = Color.White
                                )



                            ){

                                Text(
                                    text = if (fileName == "") "Dosya Seç" else fileName,
                                    modifier = Modifier.fillMaxSize(),
                                    textAlign = TextAlign.Center
                                )

                            }

                            if (selectedFileContent == null){
                                Spacer(modifier = Modifier.width(10.dp))
                                Text (
                                    text = "Dosya Seçilmedi",
                                    modifier = Modifier.align(Alignment.CenterVertically)
                                )
                            }else {
                                IconButton(
                                    onClick = {
                                        selectedFileContent = null
                                        fileName = ""
                                    },
                                    modifier = Modifier.fillMaxSize()
                                ){

                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete file"
                                    )
                                }
                            }




                        }
                    }

                    Box (
                        modifier = Modifier.fillMaxWidth().height(100.dp).offset(y = 450.dp).background(Color.LightGray, RoundedCornerShape(8.dp))
                    ){
                        BasicTextField(
                            value = description,
                            onValueChange = { description = it },
                            modifier = Modifier.fillMaxSize().offset(x = 15.dp, y = 15.dp)
                                .focusRequester(focusRequesterDesc).onFocusChanged { focusState: FocusState ->
                                    isFocusedDesc = focusState.isFocused
                                    if (isFocusedDesc && description == "Açıklama giriniz") {
                                        description = "" // Clear placeholder text when focused
                                    }
                                }
                        )
                    }

                    Text (
                        text = "   Açıklama",
                        modifier = Modifier.offset(y = 430.dp).fillMaxWidth().height(20.dp)
                    )





                    Box (
                        modifier = Modifier.fillMaxWidth().height(50.dp).offset(y = 350.dp).background(Color.LightGray, RoundedCornerShape(8.dp))
                    ){
                        BasicTextField(
                            value = title,
                            onValueChange = { title = it },
                            modifier = Modifier.fillMaxSize().offset(x = 15.dp, y = 15.dp)
                                .focusRequester(focusRequesterTitle).onFocusChanged { focusState: FocusState ->
                                    isFocusedTitle = focusState.isFocused
                                    if (isFocusedTitle && title == "Başlık giriniz") {
                                        title = "" // Clear placeholder text when focused
                                    }
                                }
                        )
                    }

                    Text (
                        text = "   Başlık",
                        modifier = Modifier.offset(y = 330.dp).fillMaxWidth().height(20.dp)
                    )





                    Box(
                        modifier = Modifier
                            .offset(y = 250.dp)
                            .fillMaxWidth()
                            .clickable { dateExpanded = !dateExpanded }
                            .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                            .padding(12.dp)

                    ) {
                        Text(text = if (checkedDate) selectedDateLast else "Bitiş tarihini seçiniz", color = Color.Black)
                    }



                    if (dateExpanded){
                        val datePickerState = rememberDatePickerState()
                        DatePickerDialog(
                            onDismissRequest = { /*TODO*/  },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                        val selectedDate = Calendar.getInstance().apply {
                                            timeInMillis = datePickerState.selectedDateMillis!!
                                        }
                                        if (selectedDate.after(Calendar.getInstance())) {
                                            Toast.makeText(
                                                context,
                                                "Seçilen tarih ${dateFormatter.format(selectedDate.time)} kaydedildi",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            selectedDateLast = dateFormatter.format(selectedDate.time)
                                            dateExpanded = false
                                            checkedDate = true
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Seçilen tarih geçmemiş bir tarih olmalı lütfen başka bir tarih seçiniz",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                ) { Text("Tamam") }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = {
                                        dateExpanded= false
                                    }
                                ) { Text("İptal") }
                            }
                        )
                        {
                            DatePicker(state = datePickerState)
                        }

                    }

                    Text (
                        text = "   Bitiş Tarihi",
                        modifier = Modifier.offset(y = 230.dp).fillMaxWidth().height(20.dp)
                    )




                    Box(
                        modifier = Modifier
                            .offset(y = 150.dp)
                            .fillMaxWidth()
                            .clickable { if (selectedClass != "Sınıf Seçiniz") courseexpanded.value = !courseexpanded.value }
                            .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                            .padding(12.dp)

                    ) {
                        selectedCourse?.let { Text(text = it, color = Color.Black) }
                    }


                    if (courseexpanded.value){
                        LazyColumn (
                            modifier = Modifier.fillMaxWidth().height(130.dp ).
                            background(Color.Transparent).
                            offset(y = 195.dp)
                        ){


                            courses.forEach { option ->
                                item {
                                    Row (
                                        modifier = Modifier.fillMaxWidth().height(40.dp).
                                        clickable(
                                            onClick = {
                                                selectedCourse = option.name
                                                selectedCourseId = option.id
                                                courseexpanded.value = false
                                                print("Clicked\n")
                                            }
                                        ).background(color = Color.White)
                                    ){
                                        Spacer(modifier = Modifier.width(10.dp))
                                        option.name?.let {
                                            Text(
                                                text = it
                                            )
                                        }
                                    }

                                }
                            }


                        }
                    }


                    Text (
                        text = "   Ders Adı",
                        modifier = Modifier.offset(y = 130.dp).fillMaxWidth().height(20.dp)
                    )





                    Box(
                        modifier = Modifier
                            .offset(y = 50.dp)
                            .fillMaxWidth()
                            .clickable { classexpanded.value = !classexpanded.value }
                            .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                            .padding(12.dp)

                    ) {
                        Text(text = selectedClass, color = Color.Black)
                    }



                    if (classexpanded.value){
                        LazyColumn (
                            modifier = Modifier.fillMaxWidth().height( 130.dp ).
                            background(Color.Transparent).
                            offset(y = 95.dp)
                        ){
                            classes.forEach { option ->
                                item {
                                    Row (
                                        modifier = Modifier.fillMaxWidth().height(40.dp).
                                        clickable(
                                            onClick = {
                                                selectedClass = option.name
                                                selectedClassId = option.id
                                                classexpanded.value = false
                                                print("Clicked\n")
                                            }
                                        ).background(color = Color.White)
                                    ){
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = option.name
                                        )
                                    }

                                }
                            }
                        }
                    }


                    Text (
                        text = "   Sınıf Adı",
                        modifier = Modifier.offset(y = 30.dp).fillMaxWidth().height(20.dp)
                    )


                }

            }




        }
    }

}else if (content_of_assignment.value == 1){

    Box (
        modifier = Modifier.fillMaxSize()
    ) {


        LazyColumn (modifier = Modifier.offset(y = 190.dp).padding(bottom = 185.dp).fillMaxSize(if (!refreshLazyColumn) 0.0f else 1.0f )) {
            if (!refreshLazyColumn){
                refreshLazyColumn = true
            }else{
                teacherAssignments.forEach { assignment ->
                    item {
                        Spacer(modifier = Modifier.height(15.dp))
                    }

                    item {

                        DisplayHomeworks(assignment, username, teacherViewModel, teacherId, changeStatus, mode)
                    }


                }

                item {
                    Spacer(modifier = Modifier.height(15.dp))
                }
            }


        }






        Card(
            modifier = Modifier.fillMaxWidth().height(if (searchExpand) 430.dp else 80.dp).offset(y = 80.dp).padding(4.dp),
            elevation = 6.dp,
            shape = RoundedCornerShape(8.dp)

        ){

            LazyColumn {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().height( 80.dp).
                        clickable{
                            searchExpand = !searchExpand
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Text(
                            text = "Ödev Ara",
                            modifier = Modifier.fillMaxWidth(0.85f),
                            textAlign = TextAlign.Center
                        )

                        Icon(
                            imageVector = if (searchExpand) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().padding(10.dp)
                        )
                    }

                    if (searchExpand){
                        Divider(modifier = Modifier.fillMaxWidth())

                        Spacer(modifier = Modifier.height(10.dp))

                        Box (
                            modifier = Modifier.fillMaxSize()
                        ){

                            Box(
                                modifier = Modifier.fillMaxWidth().height(80.dp).offset(y = 260.dp),

                                ){
                                Button(
                                    modifier = Modifier.align(Alignment.Center),
                                    onClick = {
                                        searchExpand = !searchExpand
                                        mode.value = true
                                        searchHomework = !searchHomework

                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        backgroundColor = Color(0xFF334BBE )
                                    )

                                ){
                                    Text(
                                        text = "Ara",
                                        color = Color.White
                                    )
                                }
                            }


                            Box(
                                modifier = Modifier
                                    .offset(y = 220.dp)
                                    .fillMaxWidth()
                                    .clickable { dateExpanded = !dateExpanded }
                                    .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                                    .padding(12.dp)

                            ) {
                                Text(text = if (checkedDate) selectedDateLastSearch else "Bitiş tarihini seçiniz", color = Color.Black)
                            }

                            if (dateExpanded){
                                val datePickerState = rememberDatePickerState()
                                DatePickerDialog(
                                    onDismissRequest = { /*TODO*/  },
                                    confirmButton = {
                                        TextButton(
                                            onClick = {
                                                val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                                val selectedDate = Calendar.getInstance().apply {
                                                    timeInMillis = datePickerState.selectedDateMillis!!
                                                }
                                                if (selectedDate.after(Calendar.getInstance())) {
                                                    Toast.makeText(
                                                        context,
                                                        "Seçilen tarih ${dateFormatter.format(selectedDate.time)} kaydedildi",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    selectedDateLastSearch = dateFormatter.format(selectedDate.time)
                                                    dateExpanded = false
                                                    checkedDate = true
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        "Seçilen tarih geçmemiş bir tarih olmalı lütfen başka bir tarih seçiniz",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        ) { Text("Tamam") }
                                    },
                                    dismissButton = {
                                        TextButton(
                                            onClick = {
                                                dateExpanded= false
                                            }
                                        ) { Text("İptal") }
                                    }
                                )
                                {
                                    DatePicker(state = datePickerState)
                                }

                            }

                            Text (
                                text = "   Bitiş Tarihi",
                                modifier = Modifier.offset(y = 200.dp).fillMaxWidth().height(20.dp)
                            )




                            Box(
                                modifier = Modifier
                                    .offset(y = 120.dp)
                                    .fillMaxWidth()
                                    .clickable { if (selectedClassSearch != "Sınıf Seçiniz") courseexpanded.value = !courseexpanded.value }
                                    .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                                    .padding(12.dp)

                            ) {
                                selectedCourseSearch?.let { Text(text = it, color = Color.Black) }
                            }



                            if (courseexpanded.value){
                                LazyColumn (
                                    modifier = Modifier.fillMaxWidth().height(130.dp ).
                                    background(Color.Transparent).
                                    offset(y = 165.dp)
                                ){
                                    coursesSearch.forEach { option ->
                                        item {
                                            Row (
                                                modifier = Modifier.fillMaxWidth().height(40.dp).
                                                clickable(
                                                    onClick = {
                                                        selectedCourseSearch = option.name
                                                        selectedCourseSearchId = option.id
                                                        courseexpanded.value = false
                                                        print("Clicked\n")
                                                    }
                                                ).background(color = Color.White)
                                            ){
                                                Spacer(modifier = Modifier.width(10.dp))
                                                option.name?.let {
                                                    Text(
                                                        text = it
                                                    )
                                                }
                                            }

                                        }
                                    }
                                }
                            }


                            Text (
                                text = "   Ders Adı",
                                modifier = Modifier.offset(y = 100.dp).fillMaxWidth().height(20.dp)
                            )





                            Box(
                                modifier = Modifier
                                    .offset(y = 20.dp)
                                    .fillMaxWidth()
                                    .clickable { classexpanded.value = !classexpanded.value }
                                    .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                                    .padding(12.dp)

                            ) {
                                Text(text = selectedClassSearch, color = Color.Black)
                            }



                            if (classexpanded.value){
                                LazyColumn (
                                    modifier = Modifier.fillMaxWidth().height( 130.dp ).
                                    background(Color.Transparent).
                                    offset(y = 65.dp)
                                ){
                                    classes.forEach { option ->
                                        item {
                                            Row (
                                                modifier = Modifier.fillMaxWidth().height(40.dp).
                                                clickable(
                                                    onClick = {
                                                        selectedClassSearch = option.name
                                                        selectedClassSearchId = option.id
                                                        classexpanded.value = false
                                                        print("Clicked\n")
                                                    }
                                                ).background(color = Color.White)
                                            ){
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = option.name
                                                )
                                            }

                                        }
                                    }
                                }
                            }


                            Text (
                                text = "   Sınıf Adı",
                                modifier = Modifier.fillMaxWidth().height(20.dp)
                            )
                        }
                    }
                }

            }

        }


    }

}else if (content_of_assignment.value == 2){
    Box (
        modifier = Modifier.fillMaxSize()
    ) {


        LazyColumn (modifier = Modifier.offset(y = 190.dp).padding(bottom = 185.dp).fillMaxSize(if (!refreshLazyColumn) 0.0f else 1.0f )) {
            if (!refreshLazyColumn){
                refreshLazyColumn = true
            }else{
                teacherAssignmentsPast.forEach { assignment ->
                    item {
                        Spacer(modifier = Modifier.height(15.dp))
                    }

                    item {

                        DisplayPastHomeworks(assignment, username, teacherViewModel, teacherId, changeStatus, mode, classStudents, focusRequester)
                    }


                }

                item {
                    Spacer(modifier = Modifier.height(15.dp))
                }
            }


        }




        Card(
            modifier = Modifier.fillMaxWidth().height(if (pastExpand) 430.dp else 80.dp).offset(y = 80.dp).padding(4.dp),
            elevation = 6.dp,
            shape = RoundedCornerShape(8.dp)

        ){
            LazyColumn {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().height( 80.dp).
                        clickable{
                            pastExpand = !pastExpand
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Text(
                            text = "Ödev Ara",
                            modifier = Modifier.fillMaxWidth(0.85f),
                            textAlign = TextAlign.Center
                        )

                        Icon(
                            imageVector = if (pastExpand) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().padding(10.dp)
                        )
                    }

                    if (pastExpand){
                        Divider(modifier = Modifier.fillMaxWidth())

                        Spacer(modifier = Modifier.height(10.dp))

                        Box (
                            modifier = Modifier.fillMaxSize()
                        ){

                            Box(
                                modifier = Modifier.fillMaxWidth().height(80.dp).offset(y = 260.dp),

                                ){
                                Button(
                                    modifier = Modifier.align(Alignment.Center),
                                    onClick = {
                                        pastExpand = !pastExpand
                                        mode.value = false
                                        searchHomework = !searchHomework
                                        teacherViewModel.getClassStudents(selectedClassPastId)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        backgroundColor = Color(0xFF334BBE )
                                    )

                                ){
                                    Text(
                                        text = "Ara",
                                        color = Color.White
                                    )
                                }
                            }


                            Box(
                                modifier = Modifier
                                    .offset(y = 220.dp)
                                    .fillMaxWidth()
                                    .clickable { dateExpanded = !dateExpanded }
                                    .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                                    .padding(12.dp)

                            ) {
                                Text(text = if (checkedDate) selectedDateLastPast else "Bitiş tarihini seçiniz", color = Color.Black)
                            }



                            if (dateExpanded){
                                val datePickerState = rememberDatePickerState()
                                DatePickerDialog(
                                    onDismissRequest = { /*TODO*/  },
                                    confirmButton = {
                                        TextButton(
                                            onClick = {
                                                val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                                val selectedDate = Calendar.getInstance().apply {
                                                    timeInMillis = datePickerState.selectedDateMillis!!
                                                }
                                                if (selectedDate.before(Calendar.getInstance())) {
                                                    Toast.makeText(
                                                        context,
                                                        "Seçilen tarih ${dateFormatter.format(selectedDate.time)} kaydedildi",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    selectedDateLastPast = dateFormatter.format(selectedDate.time)
                                                    dateExpanded = false
                                                    checkedDate = true
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        "Seçilen tarih geçmiş bir tarih olmalı lütfen başka bir tarih seçiniz",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        ) { Text("Tamam") }
                                    },
                                    dismissButton = {
                                        TextButton(
                                            onClick = {
                                                dateExpanded= false
                                            }
                                        ) { Text("İptal") }
                                    }
                                )
                                {
                                    DatePicker(state = datePickerState)
                                }

                            }

                            Text (
                                text = "   Bitiş Tarihi",
                                modifier = Modifier.offset(y = 200.dp).fillMaxWidth().height(20.dp)
                            )




                            Box(
                                modifier = Modifier
                                    .offset(y = 120.dp)
                                    .fillMaxWidth()
                                    .clickable { if (selectedClassPast != "Sınıf Seçiniz") courseexpanded.value = !courseexpanded.value }
                                    .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                                    .padding(12.dp)

                            ) {
                                selectedCoursePast?.let { Text(text = it, color = Color.Black) }
                            }



                            if (courseexpanded.value){
                                LazyColumn (
                                    modifier = Modifier.fillMaxWidth().height(130.dp ).
                                    background(Color.Transparent).
                                    offset(y = 165.dp)
                                ){
                                    coursesPast.forEach { option ->
                                        item {
                                            Row (
                                                modifier = Modifier.fillMaxWidth().height(40.dp).
                                                clickable(
                                                    onClick = {
                                                        selectedCoursePast = option.name
                                                        selectedCoursePastId = option.id
                                                        courseexpanded.value = false
                                                        print("Clicked\n")
                                                    }
                                                ).background(color = Color.White)
                                            ){
                                                Spacer(modifier = Modifier.width(10.dp))
                                                option.name?.let { Text ( text = it) }
                                            }

                                        }
                                    }
                                }
                            }


                            Text (
                                text = "   Ders Adı",
                                modifier = Modifier.offset(y = 100.dp).fillMaxWidth().height(20.dp)
                            )





                            Box(
                                modifier = Modifier
                                    .offset(y = 20.dp)
                                    .fillMaxWidth()
                                    .clickable { classexpanded.value = !classexpanded.value }
                                    .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                                    .padding(12.dp)

                            ) {
                                Text(text = selectedClassPast, color = Color.Black)
                            }



                            if (classexpanded.value){
                                LazyColumn (
                                    modifier = Modifier.fillMaxWidth().height( 130.dp ).
                                    background(Color.Transparent).
                                    offset(y = 65.dp)
                                ){
                                    classes.forEach { option ->
                                        item {
                                            Row (
                                                modifier = Modifier.fillMaxWidth().height(40.dp).
                                                clickable(
                                                    onClick = {
                                                        selectedClassPast = option.name
                                                        selectedClassPastId = option.id
                                                        classexpanded.value = false
                                                        print("Clicked\n")
                                                    }
                                                ).background(color = Color.White)
                                            ){
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = option.name
                                                )
                                            }

                                        }
                                    }
                                }
                            }


                            Text (
                                text = "   Sınıf Adı",
                                modifier = Modifier.fillMaxWidth().height(20.dp)
                            )
                        }
                    }
                }

            }

        }


    }
}


}
}

// Burayı halledeceğim bir şekil
@Composable
fun AddingHomework() {

}