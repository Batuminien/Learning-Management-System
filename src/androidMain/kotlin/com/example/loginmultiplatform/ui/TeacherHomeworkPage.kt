package com.example.loginmultiplatform.ui

//noinspection UsingMaterialAndMaterial3Libraries
import android.annotation.SuppressLint
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
import com.darkrockstudios.libraries.mpfilepicker.FilePicker
import com.example.loginmultiplatform.getPlatformResourceContainer
import com.example.loginmultiplatform.model.TeacherAssignmentResponse
import com.example.loginmultiplatform.model.AssignmentDocument
import com.example.loginmultiplatform.model.TeacherAssignmentRequest
import com.example.loginmultiplatform.viewmodel.TeacherAssignmentViewModel
import com.mohamedrejeb.calf.io.getName
import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import com.mohamedrejeb.calf.picker.rememberFilePickerLauncher
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.content.Context
import android.provider.OpenableColumns
import android.net.Uri
import androidx.core.net.toUri
import android.provider.DocumentsContract
import androidx.compose.ui.platform.LocalContext
import com.example.loginmultiplatform.ui.components.SharedDocument
import com.example.loginmultiplatform.ui.components.rememberDocumentManager




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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayHomeworks(assignment: TeacherAssignmentResponse){

    var displayExpand by remember { mutableStateOf(false) }
    var dateDisplayExpanded by remember { mutableStateOf(false) }
    var checkedDisplayDate by remember { mutableStateOf(false) }

    val context = LocalPlatformContext.current

    var selectedDateLastDisplay by remember { mutableStateOf("gg.aa.yyyy") }


    Card(
        modifier = Modifier.fillMaxWidth().height( if (displayExpand)  450.dp else 80.dp).padding(10.dp),
        elevation = 8.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column (
            modifier = Modifier.fillMaxSize()
        ) {
            if (!displayExpand){
                Row(
                    modifier = Modifier.fillMaxWidth().height( 80.dp).
                    clickable{
                        displayExpand = !displayExpand
                    },
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text(
                        text = "Ödev Ara",
                        modifier = Modifier.fillMaxWidth(0.85f),
                        textAlign = TextAlign.Center
                    )

                    Icon(
                        imageVector = if (displayExpand) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().padding(10.dp)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().height( 60.dp).
                    clickable{
                        displayExpand = !displayExpand
                    },
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text(
                        text = "",
                        modifier = Modifier.fillMaxWidth(0.85f),
                        textAlign = TextAlign.Center
                    )

                    Icon(
                        imageVector = if (displayExpand) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().padding(10.dp)
                    )
                }


                Divider(modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                        .padding(12.dp)

                ) {
                    Text(text = assignment.className, color = Color.Black)
                }

                Text (
                    text = "Bankaiiiii",
                    modifier = Modifier
                )

                Text (
                    text = "   Sınıf Adı",
                    modifier = Modifier.fillMaxWidth().height(20.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier

                        .fillMaxWidth()
                        .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                        .padding(12.dp)

                ) {
                    Text(text = assignment.courseName, color = Color.Black)
                }

                Text (
                    text = "Bankaiiiii",
                    modifier = Modifier
                )


                Text (
                    text = "   Ders Adı",
                    modifier = Modifier.fillMaxWidth().height(20.dp)
                )

                Spacer(modifier =Modifier.height(10.dp))



                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { dateDisplayExpanded = !dateDisplayExpanded }
                        .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                        .padding(12.dp)

                ) {
                    Text(text = if (checkedDisplayDate) selectedDateLastDisplay else "Bitiş tarihini seçiniz", color = Color.Black)
                }

                Text (
                    text = "Bankaiiiii",
                    modifier = Modifier
                )

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

                Text (
                    text = "   Bitiş Tarihi",
                    modifier = Modifier.fillMaxWidth().height(20.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))


                Box(
                    modifier = Modifier.fillMaxWidth().height(80.dp),

                    ){
                    Button(
                        modifier = Modifier.align(Alignment.Center),
                        onClick = {
                            displayExpand = !displayExpand

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










            }
        }

    }




}





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

    val isLoad by teacherViewModel.isLoading.collectAsState()


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


    // Callback to handle the selected document
    val onDocumentSelected: (SharedDocument?) -> Unit = { document ->
        if (document != null) {
            val fileName = document.fileName()
            val fileContent = document.toText() ?: "Empty content"
            Toast.makeText(context, "Selected file: $fileName", Toast.LENGTH_SHORT).show()

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


    LaunchedEffect(searchHomework){
        if (teacherId != null ) {
            println("Exploooosion")
            println(selectedCourseSearchId)
            println(selectedClassSearchId)
            println(selectedDateLastSearch)
            if ( selectedClassSearchId != -1 && selectedCourseSearchId != -1 ) {

                if (selectedDateLastSearch == "gg.aa.yyyy"){
                    teacherViewModel.fetchTeacherAssignments(
                        teacherId,
                        selectedClassSearchId,
                        selectedCourseSearchId,
                        ""
                    )
                }else{
                    teacherViewModel.fetchTeacherAssignments(
                        teacherId,
                        selectedClassSearchId,
                        selectedCourseSearchId,
                        selectedDateLastSearch
                    )
                }



                if (isLoad || errorMessage != null) {
                    Toast.makeText(
                        context,
                        "Bir hata var ${(errorMessage != null) ?: errorMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        context,
                        "Ödevler Yüklendi. Ödev miktarı: ${teacherAssignments.size}",
                        Toast.LENGTH_SHORT
                    ).show()
                    description = teacherAssignments.toString()
                }
            }
        }
    }

    LaunchedEffect(selectedClassPast){
        teacherId?.let { teacherViewModel.fetchTeacherCoursesPast(it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize().clickable{ classexpanded.value = false
                                      courseexpanded.value = false
                                      focusManagerTitle.clearFocus()
                                      focusManagerDesc.clearFocus()
                                    }

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

                            Button (
                                modifier = Modifier.offset(y = 700.dp).fillMaxWidth().height(50.dp),
                                onClick = {
                                    if (teacherId != null ) {
                                        var descriptionSent: String?
                                        if (description == "Açıklama giriniz" )
                                            descriptionSent = null
                                        else
                                            descriptionSent = description
                                        /*teacherViewModel.addAssignment(
                                            TeacherAssignmentRequest(
                                                teacherId = teacherId,
                                                title = title,
                                                description = descriptionSent,
                                                dueDate = selectedDateLast,
                                                classId = selectedClassId,
                                                courseId = selectedCourseId,
                                                document = assignmentDocument

                                                )
                                        )*/
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = Color.Transparent
                                )
                            ){
                                Text("Yükle")
                            }

                            Text(
                                modifier = Modifier.fillMaxWidth().offset(y = 650.dp),
                                text = assignmentDocument?.fileName ?: "There is no file",
                                textAlign = TextAlign.Center
                            )

                            Button (
                                modifier = Modifier.offset(y = 600.dp).fillMaxWidth().height(50.dp),
                                onClick = {

                                    documentManager.launch()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = Color.Transparent
                                )
                            ){
                                Text("press")
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

                            Text (
                                text = "Bankaiiiii",
                                modifier = Modifier.offset(y = 295.dp)
                            )

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

                            Text (
                                text = "Bankaiiiii",
                                modifier = Modifier.offset(y = 195.dp)
                            )

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

                            Text (
                                text = "Bankaiiiii",
                                modifier = Modifier.offset(y = 95.dp)
                            )

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

                LazyColumn (modifier = Modifier.offset(y = 190.dp).padding(bottom = 185.dp).fillMaxSize().background(Color.Red)) {
                    teacherAssignments.forEach { assignment ->
                        item {
                            Spacer(modifier = Modifier.height(15.dp))
                        }

                        item {

                            DisplayHomeworks(assignment)
                        }


                    }

                    item {
                        Spacer(modifier = Modifier.height(15.dp))
                    }
                }

                Text (
                    text = "Exploosion",
                    modifier = Modifier.offset(y = 170.dp).padding(start = 4.dp)
                )

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

                                    Text (
                                        text = "Bankaiiiii",
                                        modifier = Modifier.offset(y = 265.dp)
                                    )

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

                                    Text (
                                        text = "Bankaiiiii",
                                        modifier = Modifier.offset(y = 165.dp)
                                    )

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

                                    Text (
                                        text = "Bankaiiiii",
                                        modifier = Modifier.offset(y = 65.dp)
                                    )

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

                Text (
                    text = "Exploosion",
                    modifier = Modifier.offset(y = 170.dp).padding(start = 4.dp)
                )

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

                                    Text (
                                        text = "Bankaiiiii",
                                        modifier = Modifier.offset(y = 265.dp)
                                    )

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

                                    Text (
                                        text = "Bankaiiiii",
                                        modifier = Modifier.offset(y = 165.dp)
                                    )

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

                                    Text (
                                        text = "Bankaiiiii",
                                        modifier = Modifier.offset(y = 65.dp)
                                    )

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