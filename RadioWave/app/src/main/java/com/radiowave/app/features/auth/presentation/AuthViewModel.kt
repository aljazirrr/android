package com.radiowave.app.features.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radiowave.app.core.domain.model.UserProfile
import com.radiowave.app.features.auth.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val currentUser: StateFlow<UserProfile?> = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun signIn(email: String, password: String) {
        if (!validateEmail(email) || !validatePassword(password)) return
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            authRepository.signInWithEmail(email, password).fold(
                onSuccess = { _uiState.value = AuthUiState(isSuccess = true) },
                onFailure = { _uiState.value = AuthUiState(error = it.message ?: "Sign in failed") }
            )
        }
    }

    fun signUp(email: String, password: String, displayName: String) {
        if (!validateEmail(email) || !validatePassword(password) || displayName.isBlank()) {
            _uiState.value = AuthUiState(error = "All fields are required")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            authRepository.signUpWithEmail(email, password, displayName).fold(
                onSuccess = { _uiState.value = AuthUiState(isSuccess = true) },
                onFailure = { _uiState.value = AuthUiState(error = it.message ?: "Registration failed") }
            )
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            authRepository.signInWithGoogle(idToken).fold(
                onSuccess = { _uiState.value = AuthUiState(isSuccess = true) },
                onFailure = { _uiState.value = AuthUiState(error = it.message ?: "Google sign in failed") }
            )
        }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            authRepository.sendPasswordReset(email).fold(
                onSuccess = { _uiState.value = AuthUiState(isSuccess = true) },
                onFailure = { _uiState.value = AuthUiState(error = it.message) }
            )
        }
    }

    fun signOut() {
        viewModelScope.launch { authRepository.signOut() }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun validateEmail(email: String): Boolean {
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = AuthUiState(error = "Invalid email address")
            return false
        }
        return true
    }

    private fun validatePassword(password: String): Boolean {
        if (password.length < 6) {
            _uiState.value = AuthUiState(error = "Password must be at least 6 characters")
            return false
        }
        return true
    }
}
