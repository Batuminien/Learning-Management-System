package com.example.loginmultiplatform.viewmodel

import androidx.compose.material3.surfaceColorAtElevation
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loginmultiplatform.model.CourseSchedule
import com.example.loginmultiplatform.model.TeacherClassResponse
import com.example.loginmultiplatform.model.TeacherCourseResponse
import com.example.loginmultiplatform.repository.CourseScheduleRepository
import com.example.loginmultiplatform.repository.StudentPastExamResultRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.http.HTTP

class CourseScheduleViewModel : ViewModel(){

    private val repository = CourseScheduleRepository()

    private val _courseSchedule = MutableStateFlow<List<CourseSchedule>>(emptyList())
    val courseSchedule : StateFlow<List<CourseSchedule>>  = _courseSchedule


    private val _courses = MutableStateFlow<List<TeacherCourseResponse>>(emptyList())
    val courses : StateFlow<List<TeacherCourseResponse>>  = _courses

    private val _classes = MutableStateFlow<List<TeacherClassResponse>>(emptyList())
    val classes : StateFlow<List<TeacherClassResponse>>  = _classes

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun getStudentSchedule (studentId : Long){

        viewModelScope.launch {
            _errorMessage.value = null

            try{

                val schedule =  repository.getStudentSchedule(studentId = studentId)

                if (schedule.success){
                    _courseSchedule.value = schedule.data
                    println("ders programını çekme başarılı")
                }else{
                    _errorMessage.value = schedule.message
                }
            }catch( h : HttpException){
                _errorMessage.value = "Bir hata var : ${h.message()}"

            }catch (e : Exception ){
                _errorMessage.value = "Http dışı bir hata var : ${e.message}"
                println("Bir hata var ama çözemedim : ${e.message}")
            }
        }

    }

    fun getTeacherSchedule (teacherId : Long){

        viewModelScope.launch {
            _errorMessage.value = null

            try{

                val schedule =  repository.getTeacherSchedule(teacherId)

                if (schedule.success){
                    _courseSchedule.value = schedule.data
                    println("ders programını çekme başarılı")
                }else{
                    _errorMessage.value = schedule.message
                }
            }catch( h : HttpException){
                _errorMessage.value = "Bir hata var : ${h.message()}"

            }catch (e : Exception ){
                _errorMessage.value = "Http dışı bir hata var : ${e.message}"
                println("Bir hata var ama çözemedim : ${e.message}")
            }
        }

    }


    fun getTeacherCourses (teacherId : Int){

        viewModelScope.launch {
            _errorMessage.value = null

            try{

                val schedule =  repository.getTeacherCourses(teacherId)

                if (schedule.success){
                    _courses.value = schedule.data
                    println("ders programını çekme başarılı")
                }else{
                    _errorMessage.value = schedule.message
                }
            }catch( h : HttpException){
                _errorMessage.value = "Bir hata var : ${h.message()}"

            }catch (e : Exception ){
                _errorMessage.value = "Http dışı bir hata var : ${e.message}"
                println("Bir hata var ama çözemedim : ${e.message}")
            }
        }

    }

    fun getTeacherCourseClass (courseId : Int , teacherId : Int){

        viewModelScope.launch {
            _errorMessage.value = null

            try{

                val schedule =  repository.getTeacherCourseClasses(courseId, teacherId)

                if (schedule.success){
                    _classes.value = schedule.data
                    println("ders programını çekme başarılı")
                }else{
                    _errorMessage.value = schedule.message
                }
            }catch( h : HttpException){
                _errorMessage.value = "Bir hata var : ${h.message()}"

            }catch (e : Exception ){
                _errorMessage.value = "Http dışı bir hata var : ${e.message}"
                println("Bir hata var ama çözemedim : ${e.message}")
            }
        }

    }



}