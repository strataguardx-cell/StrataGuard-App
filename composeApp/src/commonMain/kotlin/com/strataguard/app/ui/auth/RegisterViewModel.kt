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

enum class AustralianState(val label: String) {
    NSW("NSW"), VIC("VIC")
}

data class RegisterUiState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val selectedState: AustralianState = AustralianState.NSW,
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val isSuccess: Boolean = false,
)

class RegisterViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onFirstNameChange(value: String) = _uiState.update { it.copy(firstName = value, errorMessage = "") }
    fun onLastNameChange(value: String)  = _uiState.update { it.copy(lastName = value, errorMessage = "") }
    fun onEmailChange(value: String)     = _uiState.update { it.copy(email = value, errorMessage = "") }
    fun onPasswordChange(value: String)  = _uiState.update { it.copy(password = value, errorMessage = "") }
    fun onConfirmPasswordChange(value: String) = _uiState.update { it.copy(confirmPassword = value, errorMessage = "") }
    fun onStateChange(state: AustralianState)  = _uiState.update { it.copy(selectedState = state) }

    fun register() {
        val state = _uiState.value
        val firstName = state.firstName.trim()
        val lastName  = state.lastName.trim()
        val email     = state.email.trim()
        val password  = state.password
        val confirm   = state.confirmPassword

        when {
            firstName.isBlank() ->
                _uiState.update { it.copy(errorMessage = "Please enter your first name.") }
            lastName.isBlank() ->
                _uiState.update { it.copy(errorMessage = "Please enter your last name.") }
            email.isBlank() ->
                _uiState.update { it.copy(errorMessage = "Please enter your email address.") }
            !email.contains("@") ->
                _uiState.update { it.copy(errorMessage = "That email address doesn't look right.") }
            password.length < 8 ->
                _uiState.update { it.copy(errorMessage = "Password must be at least 8 characters.") }
            password != confirm ->
                _uiState.update { it.copy(errorMessage = "Passwords don't match.") }
            else -> viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, errorMessage = "") }
                val displayName = "$firstName $lastName"
                when (val result = authRepository.register(email, password, displayName)) {
                    is AuthResult.Success -> _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                    is AuthResult.Error   -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }
}