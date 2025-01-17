package com.example.loginmultiplatform.ui

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.loginmultiplatform.AppContext.context
import com.example.loginmultiplatform.model.AssignmentDocument
import com.example.loginmultiplatform.model.StudentAssignmentResponse
import com.example.loginmultiplatform.viewmodel.StudentAssignmentViewModel
import java.io.File
import java.util.Date
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
actual fun StudentHomeworkPage(studentViewModel: StudentAssignmentViewModel, studentId: Int?) {
    var selectedTab by remember { mutableStateOf("Aktif Ödevler") }
    val assignmentList by studentViewModel.studentAssignments.collectAsState()

    LaunchedEffect(Unit) {
        if (studentId != null) {
            studentViewModel.fetchStudentAssignments(studentId)
        }
    }

    Column {
        // Tab Buttons
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            TabButton("Aktif Ödevler", selectedTab == "Aktif Ödevler") { selectedTab = "Aktif Ödevler" }
            TabButton("Teslim Edilmiş Ödevler", selectedTab == "Teslim Edilmiş Ödevler") { selectedTab = "Teslim Edilmiş Ödevler" }
            TabButton("Geçmiş Ödevler", selectedTab == "Geçmiş Ödevler") { selectedTab = "Geçmiş Ödevler" }
        }

        // Display assignments based on selected tab
        when (selectedTab) {
            "Aktif Ödevler" -> {
                ActiveAssignments(assignmentList)
            }
            "Teslim Edilmiş Ödevler" -> {
                SubmittedAssignments(assignmentList)
            }
            "Geçmiş Ödevler" -> {
                PastAssignments(assignmentList)
            }
        }
    }
}

@Composable
fun TabButton(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(120.dp)
            .height(76.dp)
            .padding(horizontal = 4.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = if (isSelected) Color(0xFF5D5FEF) else Color(0xFFEFEFEF)
        )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.body1, // Varsayılan tema stilini kullanarak düzenleme
            fontSize = 16.sp, // Punto boyutu kolayca ayarlanabilir
            textAlign = TextAlign.Center, // Metni ortalar
            fontWeight = FontWeight.Bold, // Kalın yazı tipi
            color = if (isSelected) Color.White else Color.Black // Seçili duruma göre metin rengi
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ActiveAssignments(assignments: List<StudentAssignmentResponse>) {
    // Filter for active assignments (mySubmission.status == "PENDING")
    val activeAssignments = assignments.filter { it.mySubmission?.status == "PENDING" }
    DisplayAssignments(activeAssignments, 0)
}
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SubmittedAssignments(assignments: List<StudentAssignmentResponse>) {
    // Filter for submitted assignments (mySubmission.status == "SUBMITTED")
    val submittedAssignments = assignments.filter { it.mySubmission?.status == "SUBMITTED" }
    DisplayAssignments(submittedAssignments, 1)
}
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PastAssignments(assignments: List<StudentAssignmentResponse>) {
    // Filter for past assignments (dueDate < current date)
    val pastAssignments = assignments.filter {
        val dueDate = it.dueDate.toDate() // Convert dueDate string to Date
        dueDate < Date()
    }
    DisplayAssignments(pastAssignments, 2)
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DisplayAssignments(assignments: List<StudentAssignmentResponse>, assignmentType: Int) {
    if (assignments.isNotEmpty()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp), // Listeye dikey boşluk
            horizontalAlignment = Alignment.CenterHorizontally // Kartların yatayda ortalanması
        ) {
            items(assignments) { assignment ->
                println(assignment)
                when (assignmentType) {
                    0 -> ActiveHomeworkCard(assignment = assignment) // Aktif ödevler için CurrentHomeworkCard çağırılır
                    1 -> SubmittedHomeworkCard(assignment = assignment) // Teslim edilmiş ödevler için SubmittedHomeworkCard çağırılır
                    2 -> PastHomeworkCard(assignment = assignment) // Geçmiş ödevler için ExpiredHomeworkCard çağırılır
                }
            }
        }
    } else {
        Text(
            text = "Ödev bulunamadı.",
            style = MaterialTheme.typography.h6,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        )
    }
}

fun String.toDate(): Date {
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return format.parse(this) ?: Date()
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ActiveHomeworkCard(assignment: StudentAssignmentResponse) {
    var isOpen by remember { mutableStateOf(false) }
    var expandedTeacherDocument by remember { mutableStateOf(false) }
    var expandedSubmissionDocument by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf(assignment.mySubmission?.comment.orEmpty()) }
    var document by remember { mutableStateOf<AssignmentDocument?>(null) }

    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm")
    val dueDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    val createdDate = assignment.createdDate.let {
        LocalDate.parse(it.substring(0, 10)).format(dueDateFormatter)
    } ?: "Bilinmiyor"
    val dueDate = assignment.dueDate.let {
        LocalDate.parse(it.substring(0, 10)).format(dueDateFormatter)
    } ?: "Bilinmiyor"
    val submissionDate = assignment.mySubmission?.submissionDate?.let {
        LocalDateTime.parse(it).format(dateFormatter)
    } ?: "Bilinmiyor"

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val viewModel : StudentAssignmentViewModel = viewModel()

    // ActivityResultLauncher tanımlanıyor
    val getContent = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val fileName = getFileNameFromUri(it, context)
            val filePath = getFilePathFromUri(it)

            // Dosya bilgilerini AssignmentDocument'e set ediyoruz
            document = AssignmentDocument(
                assignmentId = assignment.id,
                documentId = System.currentTimeMillis(), // Örnek olarak ID
                fileName = fileName,
                fileType = "pdf", // Burada dosya tipini dinamik olarak alabilirsiniz
                filePath = filePath,
                fileSize = getFileSize(uri, context),
                uploadTime = LocalDateTime.now().toString(),
                uploadedByUsername = "user" // Örnek olarak
            )
            println("Document: $document")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester) // FocusRequester bağlandı
            .clickable {
                keyboardController?.hide()
                focusRequester.freeFocus() // Artık çalışmalı
            }
    ) {
        Card(
            backgroundColor = Color(0xEFEFEFFF),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            elevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Üst Satır: Ders adı, açılır/kapanır ikon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(get_lesson_color(assignment.courseName), CircleShape)
                                .border(1.dp, Color.Black, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = assignment.courseName,
                            style = MaterialTheme.typography.h6
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF5D5FEF), CircleShape)
                            .border(1.dp, Color.Black, CircleShape)
                            .clickable { isOpen = !isOpen }
                    ) {
                        Text(
                            text = if (isOpen) "▲" else "▼",
                            color = Color.White,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                // Ödev Başlığı
                Text(
                    text = assignment.title,
                    style = MaterialTheme.typography.subtitle1,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // Açılır içerik
                if (isOpen) {
                    Spacer(modifier = Modifier.height(8.dp))
                    assignment.description?.let { Text(text = it) }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Teacher Document Alanı
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (assignment.teacherDocument == null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .border(1.dp, Color.DarkGray, shape = RoundedCornerShape(4.dp))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Döküman eklenmedi.",
                                    color = Color.DarkGray,
                                    style = MaterialTheme.typography.body2
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .border(1.dp, Color.DarkGray, shape = RoundedCornerShape(4.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = assignment.teacherDocument.fileName,
                                    style = MaterialTheme.typography.body2,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { expandedTeacherDocument = !expandedTeacherDocument },
                                    enabled = true
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Doküman seçenekleri"
                                    )
                                    DropdownMenu(
                                        expanded = expandedTeacherDocument,
                                        onDismissRequest = { expandedTeacherDocument = false }
                                    ) {
                                        DropdownMenuItem(onClick = {
                                            expandedTeacherDocument = false
                                            /*viewModel.performDocumentAction(
                                                documentId = assignment.teacherDocument.documentId,
                                                action = DocumentAction.OPEN,
                                                onSuccess = { content ->
                                                    Log.d(
                                                        "DocumentAction",
                                                        "Document opened: $content"
                                                    )
                                                    // UI'da içeriği göster
                                                },
                                                onError = { error ->
                                                    Log.e(
                                                        "DocumentAction",
                                                        "Error: ${error.message}",
                                                        error
                                                    )
                                                }
                                            )*/
                                        }) {
                                            Text("Aç")
                                        }
                                        DropdownMenuItem(onClick = {
                                            expandedTeacherDocument = false
                                            /*viewModel.performDocumentAction(
                                                documentId = assignment.teacherDocument.documentId,
                                                action = DocumentAction.DOWNLOAD,
                                                onSuccess = { message ->
                                                    Log.d("DocumentAction", message)
                                                },
                                                onError = { error ->
                                                    Log.e(
                                                        "DocumentAction",
                                                        "Error: ${error.message}",
                                                        error
                                                    )
                                                }
                                            )*/
                                        }) {
                                            Text("İndir")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Ödev Aralığı
                Text(
                    text = "Ödev Aralığı: $createdDate - $dueDate",
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(top = 8.dp)
                )
                // Teslim tarihi
                Text(
                    text = "Teslim Tarihi: $submissionDate",
                    style = MaterialTheme.typography.body2,
                    color = Color.DarkGray
                )

                if (isOpen) {

                    // Teslim Notu Alanı
                    Spacer(modifier = Modifier.height(8.dp))
                    Column {
                        Text("Teslim Notu", style = MaterialTheme.typography.subtitle2)
                        TextField(
                            value = commentText,
                            onValueChange = { updatedComment ->
                                commentText = updatedComment
                            },
                            placeholder = { Text("Notunuzu buraya yazabilirsiniz...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp)
                                .focusRequester(focusRequester)
                        )
                    }

                    // Local Document Alanı
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (document == null) {
                            Text(
                                text = "Dosya eklenmedi.",
                                style = MaterialTheme.typography.body2,
                                color = Color.DarkGray
                            )
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .border(1.dp, Color.DarkGray, shape = RoundedCornerShape(4.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = document!!.fileName,
                                    style = MaterialTheme.typography.body2,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = {
                                        expandedSubmissionDocument = !expandedSubmissionDocument
                                    },
                                    enabled = true
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Dosya seçenekleri"
                                    )
                                    DropdownMenu(
                                        expanded = expandedSubmissionDocument,
                                        onDismissRequest = { expandedSubmissionDocument = false }
                                    ) {
                                        DropdownMenuItem(onClick = {
                                            expandedSubmissionDocument = false
                                            // "Aç" işlemini burada gerçekleştirin
                                        }) {
                                            Text("Aç")
                                        }
                                        DropdownMenuItem(onClick = {
                                            expandedSubmissionDocument = false
                                            // "İndir" işlemini burada gerçekleştirin
                                        }) {
                                            Text("İndir")
                                        }
                                        DropdownMenuItem(onClick = {
                                            expandedSubmissionDocument = false
                                            document = null
                                        }) {
                                            Text("Kaldır")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Butonlar
                if(isOpen) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                // Dosya seçme işlemini başlatıyoruz
                                getContent.launch("application/pdf") // Pdf seçmek için tip belirtebiliriz
                            },
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray),
                            enabled = (document == null)
                        ) {
                            Text("Dosya Ekle")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                // "Teslim Et" işlemini burada gerçekleştirin
                                // viewModel.submitAssignment(assignment.id, (assignment.mySubmission?.comment?, document?))
                            },
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF5D5FEF))
                        ) {
                            Text("Teslim Et", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
// Dosya adını almak için yardımcı fonksiyon
fun getFileNameFromUri(uri: Uri, context: Context): String {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        it.moveToFirst()
        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        return it.getString(nameIndex)
    }
    return "Unknown"
}
// Dosya yolunu almak için yardımcı fonksiyon (Parametrede conetext vardı, kullanılmıyordu)
fun getFilePathFromUri(uri: Uri): String {
    val filePath = uri.path ?: return ""
    return filePath
}
// Dosya boyutunu almak için yardımcı fonksiyon
fun getFileSize(uri: Uri, context: Context): Long {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        it.moveToFirst()
        val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
        return it.getLong(sizeIndex)
    }
    return 0L
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SubmittedHomeworkCard(assignment: StudentAssignmentResponse) {
    var isOpen by remember { mutableStateOf(false) }
    var expandedTeacherDocument by remember { mutableStateOf(false) }
    var expandedSubmissionDocument by remember { mutableStateOf(false) }

    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm")
    val dueDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    val createdDate = assignment.createdDate.let {
        LocalDate.parse(it.substring(0, 10)).format(dueDateFormatter)
    } ?: "Bilinmiyor"
    val dueDate = assignment.dueDate.let {
        LocalDate.parse(it.substring(0, 10)).format(dueDateFormatter)
    } ?: "Bilinmiyor"
    val submissionDate = assignment.mySubmission?.submissionDate?.let {
        LocalDateTime.parse(it).format(dateFormatter)
    } ?: "Bilinmiyor"

    Card(
        backgroundColor = Color(0x8C8C8CFF),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Üst Satır: Ders adı, açılır/kapanır ikon
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(get_lesson_color(assignment.courseName), CircleShape)
                            .border(1.dp, Color.Black, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = assignment.courseName,
                        style = MaterialTheme.typography.h6
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF5D5FEF), CircleShape)
                        .border(1.dp, Color.Black, CircleShape)
                        .clickable { isOpen = !isOpen }
                ) {
                    Text(
                        text = if (isOpen) "▲" else "▼",
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            // Ödev Başlığı
            Text(
                text = assignment.title,
                style = MaterialTheme.typography.subtitle1,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Açılır içerik
            if (isOpen) {
                Spacer(modifier = Modifier.height(8.dp))
                assignment.description?.let { Text(text = it) }
                Spacer(modifier = Modifier.height(8.dp))

                // Teacher Document Alanı
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (assignment.teacherDocument == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .border(1.dp, Color.DarkGray, shape = RoundedCornerShape(4.dp))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Döküman eklenmedi.",
                                color = Color.DarkGray,
                                style = MaterialTheme.typography.body2
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .border(1.dp, Color.DarkGray, shape = RoundedCornerShape(4.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = assignment.teacherDocument.fileName,
                                style = MaterialTheme.typography.body2,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { expandedTeacherDocument = !expandedTeacherDocument },
                                enabled = true
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Doküman seçenekleri"
                                )
                                DropdownMenu(
                                    expanded = expandedTeacherDocument,
                                    onDismissRequest = { expandedTeacherDocument = false }
                                ) {
                                    DropdownMenuItem(onClick = {
                                        expandedTeacherDocument = false
                                        // "Aç" işlemini burada gerçekleştirin
                                    }) {
                                        Text("Aç")
                                    }
                                    DropdownMenuItem(onClick = {
                                        expandedTeacherDocument = false
                                        // "İndir" işlemini burada gerçekleştirin
                                    }) {
                                        Text("İndir")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Ödev Aralığı
            Text(
                text = "Ödev Aralığı: $createdDate - $dueDate",
                style = MaterialTheme.typography.body2,
                modifier = Modifier.padding(top = 8.dp)
            )
            // Teslim tarihi
            Text(
                text = "Teslim Tarihi: $submissionDate",
                style = MaterialTheme.typography.body2,
                color = Color.DarkGray
            )

            if (isOpen) {

                // Teslim Notu Alanı
                if ((assignment.mySubmission?.comment != null) && assignment.mySubmission.comment != "") {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column {
                        Text("Teslim Notu", style = MaterialTheme.typography.subtitle2)
                        Text(
                            text = assignment.mySubmission.comment,
                            style = MaterialTheme.typography.body2,
                            color = Color.DarkGray
                        )
                    }
                }

                // Document Alanı
                assignment.mySubmission?.document?.let { document ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .border(1.dp, Color.DarkGray, shape = RoundedCornerShape(4.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = document.fileName,
                                style = MaterialTheme.typography.body2,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { expandedSubmissionDocument = !expandedSubmissionDocument },
                                enabled = true
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Dosya seçenekleri"
                                )
                                DropdownMenu(
                                    expanded = expandedSubmissionDocument,
                                    onDismissRequest = { expandedSubmissionDocument = false }
                                ) {
                                    DropdownMenuItem(onClick = {
                                        expandedSubmissionDocument = false
                                        // "Aç" işlemini burada gerçekleştirin
                                    }) {
                                        Text("Aç")
                                    }
                                    DropdownMenuItem(onClick = {
                                        expandedSubmissionDocument = false
                                        // "İndir" işlemini burada gerçekleştirin
                                    }) {
                                        Text("İndir")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Butonlar
            if(isOpen) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            /* Dosya ekleme işlemi */
                        },
                        enabled = false,
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray)
                    ) {
                        Text("Dosya Ekle")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            /* Teslimi geri al işlemi */
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF5D5F00))
                    ) {
                        Text("Teslimi Geri Al", color = Color.White)
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PastHomeworkCard(assignment: StudentAssignmentResponse) {
    var isOpen by remember { mutableStateOf(false) }
    var expandedTeacherDocument by remember { mutableStateOf(false) }
    var expandedSubmissionDocument by remember { mutableStateOf(false) }

    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm")
    val dueDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    val createdDate = assignment.createdDate.let {
        LocalDate.parse(it.substring(0, 10)).format(dueDateFormatter)
    } ?: "Bilinmiyor"
    val dueDate = assignment.dueDate.let {
        LocalDate.parse(it.substring(0, 10)).format(dueDateFormatter)
    } ?: "Bilinmiyor"
    val submissionDate = assignment.mySubmission?.submissionDate?.let {
        LocalDateTime.parse(it).format(dateFormatter)
    } ?: "Bilinmiyor"

    Card(
        backgroundColor = Color(
            when (assignment.mySubmission?.status) {
                "PENDING" -> 0xEFEFEFFF
                "SUBMITTED" -> 0x8C8C8CFF
                "GRADED" -> 0x8C5D5F00
                else -> 0x8CFFFF00 // Varsayılan bir renk, hata durumuna veya eksik verilere karşı
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Üst Satır: Ders adı, açılır/kapanır ikon
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(get_lesson_color(assignment.courseName), CircleShape)
                            .border(1.dp, Color.Black, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = assignment.courseName,
                        style = MaterialTheme.typography.h6
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF5D5FEF), CircleShape)
                        .border(1.dp, Color.Black, CircleShape)
                        .clickable { isOpen = !isOpen }
                ) {
                    Text(
                        text = if (isOpen) "▲" else "▼",
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            // Ödev Başlığı
            Text(
                text = assignment.title,
                style = MaterialTheme.typography.subtitle1,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Açılır içerik
            if (isOpen) {
                Spacer(modifier = Modifier.height(8.dp))
                assignment.description?.let { Text(text = it) }
                Spacer(modifier = Modifier.height(8.dp))

                // Teacher Document Alanı
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (assignment.teacherDocument == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .border(1.dp, Color.DarkGray, shape = RoundedCornerShape(4.dp))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Döküman eklenmedi.",
                                color = Color.DarkGray,
                                style = MaterialTheme.typography.body2
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .border(1.dp, Color.DarkGray, shape = RoundedCornerShape(4.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = assignment.teacherDocument.fileName,
                                style = MaterialTheme.typography.body2,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { expandedTeacherDocument = !expandedTeacherDocument },
                                enabled = true
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Doküman seçenekleri"
                                )
                                DropdownMenu(
                                    expanded = expandedTeacherDocument,
                                    onDismissRequest = { expandedTeacherDocument = false }
                                ) {
                                    DropdownMenuItem(onClick = {
                                        expandedTeacherDocument = false
                                        // "Aç" işlemini burada gerçekleştirin
                                    }) {
                                        Text("Aç")
                                    }
                                    DropdownMenuItem(onClick = {
                                        expandedTeacherDocument = false
                                        // "İndir" işlemini burada gerçekleştirin
                                    }) {
                                        Text("İndir")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Ödev Aralığı
            Text(
                text = "Tarihi Geçti: $createdDate - $dueDate",
                color = Color.Red,
                style = MaterialTheme.typography.body2,
                modifier = Modifier.padding(top = 8.dp)
            )
            // Teslim tarihi
            Text(
                text = "Teslim Tarihi: $submissionDate",
                style = MaterialTheme.typography.body2,
                color = Color.DarkGray
            )

            if (isOpen) {
                // Teslim Notu Alanı
                if ((assignment.mySubmission?.comment != null) && assignment.mySubmission.comment != "") {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column {
                        Text("Teslim Notu", style = MaterialTheme.typography.subtitle2)
                        Text(
                            text = assignment.mySubmission.comment,
                            style = MaterialTheme.typography.body2,
                            color = Color.DarkGray
                        )
                    }
                }

                // Document Alanı
                assignment.mySubmission?.document?.let { document ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .border(1.dp, Color.DarkGray, shape = RoundedCornerShape(4.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = document.fileName,
                                style = MaterialTheme.typography.body2,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    expandedSubmissionDocument = !expandedSubmissionDocument
                                },
                                enabled = true
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Dosya seçenekleri"
                                )
                                DropdownMenu(
                                    expanded = expandedSubmissionDocument,
                                    onDismissRequest = { expandedSubmissionDocument = false }
                                ) {
                                    DropdownMenuItem(onClick = {
                                        expandedSubmissionDocument = false
                                        // "Aç" işlemini burada gerçekleştirin
                                    }) {
                                        Text("Aç")
                                    }
                                    DropdownMenuItem(onClick = {
                                        expandedSubmissionDocument = false
                                        // "İndir" işlemini burada gerçekleştirin
                                    }) {
                                        Text("İndir")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Not
            if(assignment.mySubmission?.status == "GRADED") {
                Text(
                    text = "Puan",
                    style = MaterialTheme.typography.body2,
                )
                Text(
                    text = "${assignment.mySubmission.grade} / 100",
                    style = MaterialTheme.typography.body2,
                    color = Color.DarkGray
                )
                Text(
                    text = "  ${assignment.mySubmission.feedback}",
                    style = MaterialTheme.typography.body2,
                )
            }

            // Butonlar
            if(isOpen) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            /* Dosya ekleme işlemi */
                        },
                        enabled = false, // Dosya ekleme engellenmeli
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.DarkGray)
                    ) {
                        Text("Dosya Ekle")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            /* Teslim etme işlemi */
                        },
                        enabled = false, // Teslim etme engellenmeli
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.DarkGray)
                    ) {
                        Text("Teslim Edildi"/*, color = Color.White*/)
                    }
                }
            }
        }
    }
}

enum class DocumentAction {
    OPEN, DOWNLOAD, REMOVE
}

//@Composable
//fun HomeworkCard(assignment: StudentAssignmentResponse) {
//    var isOpen by remember { mutableStateOf(false) }
//    val isSubmitted = assignment.mySubmission != null
//    val isOverdue = assignment.dueDate.toDate() < Date()
//    var expanded by remember { mutableStateOf(false) }
//    var commentText by remember { mutableStateOf(assignment.mySubmission?.comment.orEmpty()) }
//
//    Card(
//        backgroundColor = if (isSubmitted) Color(0x8C8C8CFF) else Color(0xEFEFEFFF),
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(8.dp),
//        elevation = 4.dp
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//            // Üst Satır: Ders adı, açılır/kapanır ikon
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier.weight(1f)
//                ) {
//                    Box(
//                        modifier = Modifier
//                            .size(24.dp)
//                            .background(get_lesson_color(assignment.courseName), CircleShape)
//                    )
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Text(
//                        text = assignment.courseName,
//                        style = MaterialTheme.typography.h6
//                    )
//                }
//                Box(
//                    modifier = Modifier
//                        .size(36.dp)
//                        .background(Color(0xFF5D5FEF), CircleShape)
//                        .clickable { isOpen = !isOpen }
//                ) {
//                    Text(
//                        text = if (isOpen) "▲" else "▼",
//                        color = Color.White,
//                        modifier = Modifier.align(Alignment.Center)
//                    )
//                }
//            }
//
//            // Ödev Başlığı
//            Text(
//                text = assignment.title,
//                style = MaterialTheme.typography.subtitle1,
//                modifier = Modifier.padding(top = 8.dp)
//            )
//
//            // Açılır içerik
//            if (isOpen) {
//                Spacer(modifier = Modifier.height(8.dp))
//                assignment.description?.let { Text(text = it) }
//                Spacer(modifier = Modifier.height(8.dp))
//
//                // Teacher Document Alanı
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    if (assignment.teacherDocument == null) {
//                        Box(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(vertical = 8.dp)
//                                .border(1.dp, Color.Gray, shape = RoundedCornerShape(4.dp))
//                                .padding(8.dp),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Text(
//                                text = "Döküman eklenmedi.",
//                                color = Color.Gray,
//                                style = MaterialTheme.typography.body2
//                            )
//                        }
//                    } else {
//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(vertical = 8.dp)
//                                .border(1.dp, Color.Gray, shape = RoundedCornerShape(4.dp))
//                                .padding(8.dp),
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            Text(
//                                text = assignment.teacherDocument.fileName,
//                                style = MaterialTheme.typography.body2,
//                                modifier = Modifier.weight(1f)
//                            )
//                            IconButton(
//                                onClick = { expanded = !expanded },
//                                enabled = true
//                            ) {
//                                Icon(
//                                    imageVector = Icons.Default.MoreVert,
//                                    contentDescription = "Doküman seçenekleri"
//                                )
//                                DropdownMenu(
//                                    expanded = expanded,
//                                    onDismissRequest = { expanded = false }
//                                ) {
//                                    DropdownMenuItem(onClick = {
//                                        expanded = false
//                                        // "Aç" işlemini burada gerçekleştirin
//                                    }) {
//                                        Text("Aç")
//                                    }
//                                    DropdownMenuItem(onClick = {
//                                        expanded = false
//                                        // "İndir" işlemini burada gerçekleştirin
//                                    }) {
//                                        Text("İndir")
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(8.dp))
//            }
//
//            // Teslim tarihi
//            if (isOverdue) {
//                Text(
//                    text = "Tarihi Geçti: ${assignment.dueDate}",
//                    color = Color.Red,
//                    style = MaterialTheme.typography.body2
//                )
//            } else {
//                Text(
//                    text = "Son Teslim Tarihi: ${assignment.dueDate}",
//                    style = MaterialTheme.typography.body2
//                )
//            }
//
//            // Eğer ödev teslim edilmişse teslim tarihi
//            if (isSubmitted) {
//                Text(
//                    text = "Teslim Edildi: ${assignment.mySubmission?.submissionDate ?: "Bilinmiyor"}",
//                    style = MaterialTheme.typography.body2,
//                    color = Color.Gray
//                )
//            }
//
//            // Teslim Notu Alanı
//            if (isOpen && !isSubmitted) {
//                Spacer(modifier = Modifier.height(8.dp))
//                Column {
//                    Text("Teslim Notu", style = MaterialTheme.typography.subtitle2)
//                    TextField(
//                        value = commentText, // Yeni lokal değişkeni kullanıyoruz
//                        onValueChange = { updatedComment ->
//                            // Eğer bir değişiklik yapılırsa, commentText güncellenmeli
//                            commentText = updatedComment
//                        },
//                        placeholder = { Text("Notunuzu buraya yazabilirsiniz...") },
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .heightIn(min = 56.dp),
//                        enabled = !isSubmitted // Teslim edilmediyse yazılabilir
//                    )
//                }
//            }
//            if (isOpen && isSubmitted && (assignment.mySubmission?.comment != null)) {
//                Spacer(modifier = Modifier.height(8.dp))
//                Column {
//                    Text("Teslim Notu", style = MaterialTheme.typography.subtitle2)
//                    // Eğer teslim edilmişse ve not varsa, API'den gelen notu göster
//                    Text(
//                        text = assignment.mySubmission.comment,
//                        style = MaterialTheme.typography.body2,
//                        color = Color.Gray
//                    )
//                }
//            }
//
//            // Butonlar
//            if (isOpen) {
//                Spacer(modifier = Modifier.height(8.dp))
//                Row(
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Button(
//                        onClick = {
//                            /* Dosya ekleme işlemi */
//                        },
//                        enabled = !isOverdue && !isSubmitted,
//                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray)
//                    ) {
//                        Text("Dosya Ekle")
//                    }
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Button(
//                        onClick = {
//                            /* Teslim etme işlemi */
//                        },
//                        enabled = !isOverdue,
//                        colors = ButtonDefaults.buttonColors(
//                            backgroundColor = if (isSubmitted) Color(0xFF5D5F00) else Color(0xFF5D5FEF)
//                        )
//                    ) {
//                        Text(if (isSubmitted) "Teslimi Geri Al" else "Teslim Et", color = Color.White)
//                    }
//                }
//            }
//        }
//    }
//}