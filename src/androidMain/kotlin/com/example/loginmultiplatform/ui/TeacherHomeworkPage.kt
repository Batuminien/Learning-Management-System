package com.example.loginmultiplatform.ui

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.LocalPlatformContext
import com.example.loginmultiplatform.AppContext.context
import com.example.loginmultiplatform.getPlatformResourceContainer
import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import com.mohamedrejeb.calf.picker.rememberFilePickerLauncher
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.nio.file.WatchEvent
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun get_buttons(content_of_assignment: MutableState<Int>, content_value: Int , buttonText : String){

    IconButton (
        modifier = Modifier.height(60.dp).width(105.dp).
        background( color = if (content_of_assignment.value == content_value)  Color(0xFF5270FF) else Color.White , RoundedCornerShape(10.dp) ),
        onClick = {content_of_assignment.value = content_value; print("Clicked\n") }

    ){
        Row (
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ){

            Icon(
                painter = painterResource(id = if (content_of_assignment.value == content_value ) getPlatformResourceContainer().eyeOpen else getPlatformResourceContainer().eyeClose ),
                contentDescription = buttonText,
                modifier = Modifier.size(40.dp).padding(7.dp)
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





@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun TeacherHomeworkPage (title: String) {
    var content_of_assignment = remember { mutableStateOf(1) }

    var classexpanded by remember { mutableStateOf(false) }
    var selectedClass by remember { mutableStateOf("Sınıf Seçiniz") }
    val classoptions = listOf("Sınıf 1", "Sınıf 2", "Sınıf 3", "Sınıf 4", "Sınıf 5")
    var courseexpanded by remember { mutableStateOf(false) }
    var selectedCourse by remember { mutableStateOf("Ders Seçiniz") }
    val courseoptions = listOf("Mat", "Türkçe", "İngilizce", "Fizik", "Kimya")
    var selectedDateLast by remember { mutableStateOf("gg.aa.yyyy") }





    var checkedDate by remember { mutableStateOf(false)}


    val date = remember {
        Calendar.getInstance()
    }
    val datePickerState = rememberDatePickerState(


    )
    var dateExpanded by remember { mutableStateOf(false) }



    val focusRequesterTitle = remember { FocusRequester() }
    val focusManagerTitle: FocusManager = LocalFocusManager.current
    var title by remember { mutableStateOf("Başlık giriniz") }
    var isFocusedTitle by remember { mutableStateOf(false) }


    val focusRequesterDesc = remember { FocusRequester() }
    val focusManagerDesc: FocusManager = LocalFocusManager.current
    var description by remember { mutableStateOf("Açıklama giriniz") }
    var isFocusedDesc by remember { mutableStateOf(false) }


    val scope = rememberCoroutineScope()
    val context = LocalPlatformContext.current

    val pickerLauncher = rememberFilePickerLauncher(
        type = FilePickerFileType.Pdf,
        selectionMode = FilePickerSelectionMode.Single,
        onResult = { files ->
            // Process the selected file(s)
            if (files.isNotEmpty()) {
                val pickedFileUri = files.firstOrNull()?.uri

                pickedFileUri?.let { uri ->
                    // Launch a coroutine to handle file saving
                    scope.launch {
                        val savedFile = saveFileFromUri(context, uri, "picked_file.pdf")
                        if (savedFile != null) {
                            println("File saved successfully at: ${savedFile.absolutePath}")
                        } else {
                            println("Failed to save file")
                        }
                    }
                }
            }
        }
    )



    Box(
        modifier = Modifier
            .fillMaxSize().clickable{ classexpanded = false
                                      courseexpanded = false
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

                get_buttons(content_of_assignment,0 , "Ödev Ekle")

                get_buttons(content_of_assignment,1 , "Aktif Ödevler")

                get_buttons(content_of_assignment,2 ,"Geçmiş Ödevler")

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
                                modifier = Modifier.offset(y = 600.dp).fillMaxWidth().height(50.dp),
                                onClick = {
                                    pickerLauncher.launch()
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

                                DatePickerDialog(
                                    onDismissRequest = { /*TODO*/  },
                                    confirmButton = {
                                        TextButton(
                                            onClick = {
                                                val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
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
                                    .clickable { if (selectedClass != "Sınıf Seçiniz") courseexpanded = !courseexpanded }
                                    .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                                    .padding(12.dp)

                            ) {
                                Text(text = selectedCourse, color = Color.Black)
                            }

                            Text (
                                text = "Bankaiiiii",
                                modifier = Modifier.offset(y = 195.dp)
                            )

                            if (courseexpanded){
                                LazyColumn (
                                    modifier = Modifier.fillMaxWidth().height(130.dp ).
                                    background(Color.Transparent).
                                    offset(y = 195.dp)
                                ){
                                    courseoptions.forEach { option ->
                                        item {
                                            Row (
                                                modifier = Modifier.fillMaxWidth().height(40.dp).
                                                clickable(
                                                    onClick = {
                                                        selectedCourse = option
                                                        courseexpanded = false
                                                        print("Clicked\n")
                                                    }
                                                ).background(color = Color.White)
                                            ){
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = option
                                                )
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
                                    .clickable { classexpanded = !classexpanded }
                                    .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                                    .padding(12.dp)

                            ) {
                                Text(text = selectedClass, color = Color.Black)
                            }

                            Text (
                                text = "Bankaiiiii",
                                modifier = Modifier.offset(y = 95.dp)
                            )

                            if (classexpanded){
                                LazyColumn (
                                    modifier = Modifier.fillMaxWidth().height( 130.dp ).
                                    background(Color.Transparent).
                                    offset(y = 95.dp)
                                ){
                                    classoptions.forEach { option ->
                                        item {
                                            Row (
                                                modifier = Modifier.fillMaxWidth().height(40.dp).
                                                clickable(
                                                    onClick = {
                                                        selectedClass = option
                                                        classexpanded = false
                                                        print("Clicked\n")
                                                    }
                                                ).background(color = Color.White)
                                            ){
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = option
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

        }


    }
}

// Burayı halledeceğim bir şekil
@Composable
fun AddingHomework() {

}