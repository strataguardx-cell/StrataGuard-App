package com.strataguard.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.strataguard.app.data.auth.AuthRepository
import com.strataguard.app.data.auth.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val isSuccess: Boolean = false,
)

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, errorMessage = "") }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = "") }
    }

    fun signIn() {
        val state = _uiState.value
        val email = state.email.trim()
        val password = state.password

        if (email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter your email address.") }
            return
        }
        if (password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter your password.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = "") }
            when (val result = authRepository.signIn(email, password)) {
                is AuthResult.Success -> _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                is AuthResult.Error  -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }
}