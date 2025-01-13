package com.example.loginmultiplatform.viewmodel

import androidx.compose.material3.surfaceColorAtElevation
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loginmultiplatform.model.CourseSchedule
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



}