package com.example.loginmultiplatform.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loginmultiplatform.model.LoginData
import com.example.loginmultiplatform.network.TokenManager
import com.example.loginmultiplatform.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val userRepository = UserRepository()

    private val _studentId = MutableStateFlow<Int?>(null)
    val studentId: StateFlow<Int?> get() = _studentId

    private val _username = MutableStateFlow<String?>(null)
    val username: StateFlow<String?> get() = _username

    fun login(
        username: String,
        password: String,
        onSuccess: (LoginData) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = userRepository.login(username, password)
                TokenManager.setToken(response.accessToken)
                if (response.accessToken.isNotBlank() && response.role.isNotEmpty()) {
                    _studentId.value = response.id
                    _username.value = response.name
                    onSuccess(response)
                } else {
                    onError("Invalid token received from server")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error occurred")
            }
        }
    }
}