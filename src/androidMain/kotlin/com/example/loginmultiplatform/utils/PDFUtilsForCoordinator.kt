package com.example.loginmultiplatform.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.FileProvider
import com.example.loginmultiplatform.model.AttendanceResponse
import java.io.File
import java.io.FileOutputStream
import com.example.loginmultiplatform.R
import com.example.loginmultiplatform.model.AttendanceStats
import com.example.loginmultiplatform.model.StudentCourseResponse
import com.example.loginmultiplatform.model.TeacherClassResponse
import java.text.SimpleDateFormat
import java.util.Date
import java.util.DoubleSummaryStatistics
import java.util.Locale

fun CreateAttendancePDFforCoordinator(
    context: Context,
    groupedData: Map<Long, List<AttendanceResponse>>,
    startDate: String,
    endDate: String,
    statistics: List<AttendanceStats>,
    courses: List<StudentCourseResponse>,
    classId: Int,
    courseId: Int,
    studentName: String,
    allClasses: List<TeacherClassResponse>
) {

    val pdfDocument = PdfDocument()
    val pageWidth = 595
    val pageHeight = 842
    val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    //renk sabitleri
    val primaryBlue = Color.rgb(51,75,190)
    val darkTextColor = Color.BLACK
    val tableHeaderColor = Color.rgb(51,75,190)
    val tableRowColor2 = Color.rgb(51,75,190)
    val tableRowColor1 = Color.rgb(237,242,254)
    val whiteColor = Color.WHITE

    val col1Right = 180f
    val col2Right = 380f

    //header arka plan
    paint.style = Paint.Style.FILL
    paint.color = primaryBlue
    canvas.drawRect(0f, 0f, pageWidth.toFloat(), 100f, paint)

    //solda kurum logosu
    val sclogoBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.logo)
    val scscaledLogo = Bitmap.createScaledBitmap(sclogoBitmap, 60, 60, false)
    canvas.drawBitmap(scscaledLogo, 20f, 20f, paint)

    //leranova logo
    val logoBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.app_logo)
    val scaledLogo = Bitmap.createScaledBitmap(logoBitmap, 150, 150, false)
    canvas.drawBitmap(scaledLogo, (pageWidth - scaledLogo.width)/2f, -20f, paint)

    // Sağ üst köşede Öğrenci Adı
    paint.textSize = 30f
    paint.color = Color.WHITE
    paint.typeface = ResourcesCompat.getFont(context, R.font.montserrat_bold)
    //val studentName = statistics.firstOrNull()?.studentName ?: "Öğrenci Adı Bulunamadı"
    val studentNameWidth = paint.measureText(studentName)
    canvas.drawText(studentName, pageWidth - studentNameWidth - 20f, 65f, paint)

    // Başlığın altındaki bölüme geliyoruz
    var yOffset = 130f

    // "YOKLAMA RAPORU" başlığı
    paint.color = darkTextColor
    paint.textSize = 20f
    paint.typeface = ResourcesCompat.getFont(context, R.font.montserrat_black)
    val mainTitle = "YOKLAMA RAPORU"
    val mainTitleWidth = paint.measureText(mainTitle)
    canvas.drawText(mainTitle, (pageWidth - mainTitleWidth) / 2, yOffset, paint)
    yOffset += 25f

    // Oluşturulma tarihi ve veri aralığı
    paint.textSize = 12f
    paint.typeface = ResourcesCompat.getFont(context, R.font.montserrat_bold)
    val creationDate = "Oluşturulma Tarihi: ${SimpleDateFormat("d MMMM yyyy", Locale("tr")).format(Date())}"
    canvas.drawText(creationDate, 20f, yOffset, paint)
    yOffset += 15f

    val formattedStartDate = SimpleDateFormat("d MMMM yyyy", Locale("tr")).format(SimpleDateFormat("yyyy-MM-dd", Locale("tr")).parse(startDate))
    val formattedEndDate = SimpleDateFormat("d MMMM yyyy", Locale("tr")).format(SimpleDateFormat("yyyy-MM-dd", Locale("tr")).parse(endDate))
    val dataRange = "Yoklama Verileri Aralığı: $formattedStartDate - $formattedEndDate"
    canvas.drawText(dataRange, 20f, yOffset, paint)
    yOffset += 20f

    val className = allClasses.find { it.classId == classId }
    if (className != null) {
        canvas.drawText("Sınıfı: ${className.name}", 20f, yOffset, paint)
    }
    yOffset += 25f

    // İnce çizgi
    paint.color = primaryBlue
    paint.strokeWidth = 3f
    canvas.drawLine(20f, yOffset, pageWidth - 20f, yOffset, paint)
    yOffset += 30f

    val tableLeft = 20f
    val tableRight = pageWidth - 20f
    val tableHeaderHeight = 30f

    // Her bir ders için tablo çizelim
    // Yazı tiplerini tablo için normal, başlık için bold yapabiliriz.
    groupedData.forEach { (courseIdKey, attendanceList ) ->
        // Ders başlığı
        paint.color = darkTextColor
        paint.textSize = 16f
        paint.typeface = ResourcesCompat.getFont(context, R.font.montserrat_black)

        val dersTitle = courses.find { it.id == courseId }
        if (dersTitle != null) {
            canvas.drawText(dersTitle.name, 20f, yOffset, paint)
        } else {
            canvas.drawText("Kurs Bulunamadı", 20f, yOffset, paint)
        }
        yOffset += 15f

        // Tablo başlıkları
        // Başlık arka planı
        paint.style = Paint.Style.FILL
        paint.color = tableHeaderColor
        canvas.drawRect(tableLeft, yOffset, tableRight, yOffset + tableHeaderHeight, paint)

        // Başlık yazıları (Durum | Tarih | Açıklama)
        paint.color = whiteColor
        paint.textSize = 12f
        paint.typeface = ResourcesCompat.getFont(context, R.font.montserrat_black)
        val durumX = 40f
        val tarihX = 200f
        val aciklamaX = 400f
        canvas.drawText("Durum", durumX, yOffset + 20f, paint)
        canvas.drawText("Tarih", tarihX, yOffset + 20f, paint)
        canvas.drawText("Açıklama", aciklamaX, yOffset + 20f, paint)

        paint.color = Color.WHITE
        paint.strokeWidth = 2f
        canvas.drawLine(col1Right, yOffset, col1Right, yOffset + tableHeaderHeight, paint)
        canvas.drawLine(col2Right, yOffset, col2Right, yOffset + tableHeaderHeight, paint)
        yOffset += tableHeaderHeight

        // Tablo satırları

        val rowHeight = 30f

        if(attendanceList.isEmpty()) {
            paint.typeface = ResourcesCompat.getFont(context, R.font.montserrat_bolditalic)
            paint.color = Color.BLACK
            paint.textSize = 12f
            canvas.drawText("Yoklama kaydı bulunamadı", durumX, yOffset + 20f, paint)
        } else {
            attendanceList.forEachIndexed { index, attendance ->
                val top = yOffset
                val bottom = yOffset + rowHeight
                //Log.e("pdf", "attendance = ${attendance}")
                paint.typeface = ResourcesCompat.getFont(context, R.font.montserrat_medium)
                // Satır arka plan rengi alternatifi
                paint.style = Paint.Style.FILL
                //paint.color = if (index % 2 == 0) tableRowColor1 else tableRowColor2
                paint.color = tableRowColor1
                canvas.drawRect(tableLeft, top, tableRight, bottom, paint)

                paint.style = Paint.Style.STROKE
                paint.color = tableHeaderColor
                paint.strokeWidth = 1f
                canvas.drawRect(tableLeft + 1f, top, tableRight - 1f, bottom - 1f, paint)

                paint.style = Paint.Style.FILL

                // Metinler
                //paint.color = if (index % 2 == 0) Color.BLACK else Color.WHITE
                paint.color = Color.BLACK
                paint.textSize = 12f
                val attendanceDate =
                    SimpleDateFormat("yyyy-MM-dd", Locale("tr")).parse(attendance.date)
                val outputFormat = SimpleDateFormat("d MMMM yyyy EEEE", Locale("tr"))
                val formattedDate = if (attendanceDate != null)
                    outputFormat.format(attendanceDate)
                else
                    attendance.date

                canvas.drawText(attendance.status, durumX, top + 20f, paint)
                canvas.drawText(formattedDate, tarihX, top + 20f, paint)
                canvas.drawText(
                    attendance.comment ?: "Açıklama yapılmadı.",
                    aciklamaX,
                    top + 20f,
                    paint
                )

                yOffset += rowHeight

            }
        }

        yOffset += 20f

        // İlgili dersin istatistikleri (Sadece bu dersin istatistikleri)
        val filteredStats = statistics.filter { it.classId == classId && it.courseId == courseId }
        if (filteredStats.isNotEmpty()) {
            val stats = filteredStats.first() // Her ders için tek bir istatistik kabul ediyoruz

            paint.color = Color.BLACK
            paint.typeface = ResourcesCompat.getFont(context, R.font.montserrat_bold)

            canvas.drawText("Toplam Ders Sayısı: ${stats.totalClasses}", 40f, yOffset, paint)
            canvas.drawText("Devam Oranı: ${stats.attendancePercentage}%", 40f, yOffset + 15f, paint)
            canvas.drawText("Katıldığı Ders Sayısı: ${stats.presentCount}", 40f, yOffset + 30f, paint)
            canvas.drawText("Gelmediği Ders Sayısı: ${stats.absentCount}", 40f, yOffset + 45f, paint)
            canvas.drawText("Geç Kaldığı Ders Sayısı: ${stats.lateCount}", 40f, yOffset + 60f, paint)
            yOffset += 100f
        } else {
            // Eğer istatistik bulunamazsa
            paint.typeface = ResourcesCompat.getFont(context, R.font.montserrat_bolditalic)
            canvas.drawText("İstatistik kaydı bulunamadı.", 40f, yOffset+30, paint)
            yOffset += 60f
        }
    }

    pdfDocument.finishPage(page)

    // Dosyayı yazma ve açma
    val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    //val file = File(downloadDir, "${studentName}_${SimpleDateFormat("d_MMMM_yyyy", Locale("tr")).format(Date())}.pdf")
    val file = File(downloadDir, "${studentName}_Yoklama_Raporu.pdf")

    try {
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()
        openOrSharePDF(context, file)
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "PDF Oluşturulurken Hata Oluştu.", Toast.LENGTH_LONG).show()
    }
}