package com.example.loginmultiplatform.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.loginmultiplatform.viewmodel.LoginViewModel
import android.content.ContentResolver
import android.graphics.Paint.Align
import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalOf
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.loginmultiplatform.viewmodel.StudentPastExamResultsViewModel
import coil3.compose.LocalPlatformContext
import okhttp3.internal.platform.android.SocketAdapter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@SuppressLint("NewApi")
fun reformatDate(input: String): String {
    // Define input and output formats
    val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // Parse and format the date
    val date = LocalDateTime.parse(input, inputFormatter)
    return date.format(outputFormatter)
}

@Composable
actual fun StudentPastExamPage (loginViewModel: LoginViewModel, navController: NavController, studentPastExamViewModel : StudentPastExamResultsViewModel){
    val username by loginViewModel.username.collectAsState()

    val studentId by loginViewModel.studentId.collectAsState()

    val pastExams by studentPastExamViewModel.pastExams.collectAsState()

    var loading by remember { mutableStateOf(false)}

    val errorMessage by studentPastExamViewModel.errorMessage.collectAsState()


    val context = LocalPlatformContext.current



    LaunchedEffect(Unit){
        loading = true
        studentId?.let { studentPastExamViewModel.fetchStudentPastExams(it.toLong()) }
    }


    LaunchedEffect(errorMessage){
        if (errorMessage != null){
            Toast.makeText(
                context,
                "Sınavlar çekilirken bir hata oluştu : $errorMessage",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    LaunchedEffect(pastExams){
        if (pastExams.isNotEmpty()){
            loading = false
        }
    }


    if (pastExams.isEmpty() ){
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            (if (errorMessage == null) "Geçmiş sınav bulunmamaktadır" else errorMessage)?.let {
                Text (
                    text = it
                )
            }
        }
    }else {
        if (loading){
            Toast.makeText(
                context,
                "Sınavlar yükleniyor",
                Toast.LENGTH_SHORT
            ).show()
        }else{
            println(pastExams)
            LazyColumn {
                pastExams.forEach { exams->
                    item{
                        var displayExpanded by remember { mutableStateOf(false) }

                        val animatedHeight by animateDpAsState(
                            targetValue = if (displayExpanded) 700.dp else 100.dp,
                            animationSpec = tween(durationMillis = 300)
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth().height( animatedHeight ).padding(12.dp),
                            elevation = 5.dp,
                            shape = RoundedCornerShape(10.dp)
                        ){
                            Column{
                                Row(
                                    modifier = Modifier.fillMaxWidth().height( 80.dp).clickable{ displayExpanded = !displayExpanded},
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween

                                ){

                                    Icon(
                                        imageVector = Icons.Default.Analytics,
                                        contentDescription = "Exam",
                                        modifier = Modifier.fillMaxWidth(0.2f).size(30.dp)
                                    )

                                    Text(
                                        text = exams.pastExam.examType + "   ",
                                        color = Color.Blue,
                                        fontWeight = FontWeight.Bold,
                                    )


                                    Text (
                                        text = exams.pastExam.name,
                                        modifier = Modifier.fillMaxWidth(0.38f),
                                        fontSize = 13.sp
                                    )

                                    Text (
                                        text = reformatDate(exams.pastExam.date),
                                        fontSize = 13.sp
                                    )


                                    Icon(
                                        imageVector = if (displayExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize().padding(18.dp)
                                    )



                                }

                                if (displayExpanded){
                                    Divider(thickness = 3.dp, color = Color.Black ,modifier = Modifier.height(2.dp))
                                    Spacer(modifier = Modifier.height(20.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth().height(20.dp),
                                        horizontalArrangement = Arrangement.SpaceAround,
                                        verticalAlignment = Alignment.CenterVertically
                                    ){
                                        Row(
                                            modifier = Modifier.fillMaxHeight(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ){
                                            Box(
                                                modifier = Modifier.size(height = 20.dp, width = 25.dp).background(Color(0xFF672CF8), RoundedCornerShape(8.dp))
                                            )

                                            Text(
                                                text = "  Doğru"
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxHeight(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ){
                                            Box(
                                                modifier = Modifier.size(height = 20.dp, width = 25.dp).background(Color(0xFF0B3EF9), RoundedCornerShape(8.dp))
                                            )

                                            Text(
                                                text = "  Yanlış"
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxHeight(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ){
                                            Box(
                                                modifier = Modifier.size(height = 20.dp, width = 25.dp).background(Color(0xFFB71C1C), RoundedCornerShape(8.dp))
                                            )
                                            Text(
                                                text = "  Boş"
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxHeight(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ){
                                            Box(
                                                modifier = Modifier.size(height = 20.dp, width = 25.dp).background(Color(0xFFB3E5FC), RoundedCornerShape(8.dp))
                                            )

                                            Text(
                                                text = "  Net"
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(25.dp))


                                    Box(
                                        modifier = Modifier.fillMaxWidth().height(500.dp)
                                    ){

                                        for( y in 0..240 step 60){
                                            println("Y : $y")
                                            Row(
                                                modifier = Modifier.fillMaxWidth().offset(y = y.dp, x = 5.dp),
                                                verticalAlignment = Alignment.Top
                                            ){
                                                Text(
                                                    text = " ${40 - (y/6)}  ",
                                                    modifier = Modifier.width(55.dp)

                                                )

                                                Divider(thickness = 2.dp, color = Color.LightGray ,modifier = Modifier.fillMaxWidth() )

                                            }
                                        }


                                        for ( y in 280..430 step 50) {
                                            Row(
                                                modifier = Modifier.offset(y = y.dp).width(55.dp).height(50.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ){
                                                Text(
                                                    text = when(y) {
                                                        280  -> "Doğru "
                                                        330 -> "Yanlış"
                                                        380 -> "Boş   "
                                                        else -> "Net   "
                                                    },

                                                    modifier = Modifier.rotate(-45f).fillMaxWidth(0.9f)

                                                )

                                                Divider(thickness = 5.dp ,modifier = Modifier.fillMaxHeight().width(2.dp))
                                            }
                                        }

                                        LazyRow (
                                            modifier = Modifier.fillMaxHeight().fillMaxWidth().offset(x = 55.dp).background(Color.Transparent),
                                        ){
                                            exams.subjectResults.forEach{ subject ->
                                                item {



                                                    Column(
                                                        modifier = Modifier.width(100.dp)
                                                    ){
                                                        Row(
                                                            modifier = Modifier.width(75.dp).height(240.dp).background(Color.Transparent),
                                                            verticalAlignment = Alignment.Top
                                                        ){
                                                            Spacer(modifier = Modifier.width(30.dp))

                                                            Box(
                                                                modifier = Modifier.width(20.dp).fillMaxHeight(),

                                                                ){
                                                                Box(
                                                                    modifier = Modifier.offset(y = (240 - subject.correctAnswers * 6).dp).width(20.dp).height((subject.correctAnswers * 6).dp).background(color = Color(0xFF672CF8))
                                                                )

                                                                Box(
                                                                    modifier = Modifier.offset(y = (240-(subject.correctAnswers*6) - (subject.incorrectAnswers*6)).dp).width(20.dp).height((subject.incorrectAnswers*6 ).dp).background(color = Color(0xFF0B3EF9))
                                                                )

                                                                Box(
                                                                    modifier = Modifier.offset(y = (240-(subject.correctAnswers*6) - (subject.incorrectAnswers*6) - (subject.blankAnswers*6)).dp).width(20.dp).height((subject.blankAnswers*6 ).dp).background(color = Color(0xFFB71C1C))
                                                                )

                                                                println("Son durum : ${(240-(subject.correctAnswers*6) - (subject.incorrectAnswers*6) - (subject.blankAnswers*6))}")
                                                            }

                                                            Spacer(modifier = Modifier.width(5.dp))
                                                            Box(
                                                                modifier = Modifier.width(20.dp).fillMaxHeight()
                                                            ){
                                                                Box(
                                                                    modifier = Modifier.offset(y = (240.0-(subject.netScore*6.0)).dp).width(20.dp).height( (subject.netScore*6 ).dp).background(color = Color(0xFFB3E5FC))
                                                                )

                                                            }


                                                        }

                                                        Box{
                                                            Text (
                                                                text = subject.subjectName,
                                                                modifier = Modifier.width(100.dp).height(50.dp).align(Alignment.Center),
                                                                textAlign = TextAlign.Center
                                                            )
                                                        }




                                                        Spacer(modifier = Modifier.height(15.dp))

                                                        for ( i in 1..4 ){
                                                            Text(
                                                                text = "${when(i){
                                                                    1 -> subject.correctAnswers
                                                                    2 -> subject.incorrectAnswers
                                                                    3 -> subject.blankAnswers
                                                                    else -> subject.netScore
                                                                }}",
                                                                modifier = Modifier.width(100.dp),
                                                                textAlign = TextAlign.Center
                                                            )

                                                            Spacer(modifier = Modifier.height(30.dp))
                                                        }


                                                    }
                                                    Spacer(modifier = Modifier.width(25.dp))



                                                }

                                            }

                                            item {
                                                Spacer(modifier = Modifier.width(25.dp))
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

}
