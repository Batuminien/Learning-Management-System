package com.example.loginmultiplatform.viewmodel

import androidx.lifecycle.ViewModel
import com.example.loginmultiplatform.model.StudentExamResultsResponses
import com.example.loginmultiplatform.repository.StudentPastExamResultRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.HttpException

class StudentPastExamResultsViewModel : ViewModel(){

    private val repository = StudentPastExamResultRepository()

    private val _pastExams = MutableStateFlow<List<StudentExamResultsResponses>>(emptyList())
    val pastExams : StateFlow<List<StudentExamResultsResponses>> = _pastExams

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun fetchStudentPastExams (studentId : Long){
        viewModelScope.launch{
            _errorMessage.value = null


            try{
                val pastExams = repository.fetchStudentPastExamResults(studentId)

                if (pastExams.success){
                    _pastExams.value = pastExams.data

                }else{
                    _errorMessage.value = "Bir hata oluştu ${pastExams.message}"

                }

            }catch (h : HttpException){
                _errorMessage.value = "Bir hata oluştu : ${h.message()}"
                println("Sınavları çekerken bir hata oluştu : ${h.message()}")
            }catch (e : Exception){
                _errorMessage.value = "Bir hata oluştu : ${e.message}"
            }
        }

    }

}