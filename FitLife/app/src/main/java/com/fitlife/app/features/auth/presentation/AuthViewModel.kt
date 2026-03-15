package com.fitlife.app.features.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitlife.app.core.domain.model.User
import com.fitlife.app.core.utils.Resource
import com.fitlife.app.core.utils.isValidEmail
import com.fitlife.app.core.utils.isValidPassword
import com.fitlife.app.features.auth.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val currentUser: User? = null,
    val error: String? = null,
    val successMessage: String? = null,
    val isAuthenticated: Boolean = false
)

data class LoginFormState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isFormValid: Boolean = false
)

data class RegisterFormState(
    val displayName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isFormValid: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _loginForm = MutableStateFlow(LoginFormState())
    val loginForm: StateFlow<LoginFormState> = _loginForm.asStateFlow()

    private val _registerForm = MutableStateFlow(RegisterFormState())
    val registerForm: StateFlow<RegisterFormState> = _registerForm.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.getCurrentUserFlow().collect { user ->
                _uiState.update { it.copy(
                    currentUser = user,
                    isAuthenticated = user != null
                )}
            }
        }
    }

    // ─── Login ─────────────────────────────────────────────────────────────────

    fun onLoginEmailChange(email: String) {
        _loginForm.update { form ->
            form.copy(
                email = email,
                emailError = if (email.isNotEmpty() && !email.isValidEmail()) "Email invalid" else null
            ).let { it.copy(isFormValid = validateLoginForm(it)) }
        }
    }

    fun onLoginPasswordChange(password: String) {
        _loginForm.update { form ->
            form.copy(
                password = password,
                passwordError = if (password.isNotEmpty() && password.length < 6) "Parola prea scurtă" else null
            ).let { it.copy(isFormValid = validateLoginForm(it)) }
        }
    }

    fun signIn() {
        viewModelScope.launch {
            val form = _loginForm.value
            if (!validateLoginForm(form)) return@launch

            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.signIn(form.email.trim(), form.password)) {
                is Resource.Success -> _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }
                is Resource.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.signInWithGoogle(idToken)) {
                is Resource.Success -> _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }
                is Resource.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    // ─── Register ──────────────────────────────────────────────────────────────

    fun onRegisterNameChange(name: String) {
        _registerForm.update { form ->
            form.copy(
                displayName = name,
                nameError = if (name.isNotEmpty() && name.length < 2) "Numele prea scurt" else null
            ).let { it.copy(isFormValid = validateRegisterForm(it)) }
        }
    }

    fun onRegisterEmailChange(email: String) {
        _registerForm.update { form ->
            form.copy(
                email = email,
                emailError = if (email.isNotEmpty() && !email.isValidEmail()) "Email invalid" else null
            ).let { it.copy(isFormValid = validateRegisterForm(it)) }
        }
    }

    fun onRegisterPasswordChange(password: String) {
        _registerForm.update { form ->
            form.copy(
                password = password,
                passwordError = when {
                    password.isEmpty() -> null
                    password.length < 8 -> "Minim 8 caractere"
                    !password.any { it.isUpperCase() } -> "Cel puțin o literă mare"
                    !password.any { it.isDigit() } -> "Cel puțin o cifră"
                    else -> null
                }
            ).let { it.copy(isFormValid = validateRegisterForm(it)) }
        }
    }

    fun onRegisterConfirmPasswordChange(confirmPassword: String) {
        _registerForm.update { form ->
            form.copy(
                confirmPassword = confirmPassword,
                confirmPasswordError = if (confirmPassword.isNotEmpty() && confirmPassword != form.password)
                    "Parolele nu coincid" else null
            ).let { it.copy(isFormValid = validateRegisterForm(it)) }
        }
    }

    fun register() {
        viewModelScope.launch {
            val form = _registerForm.value
            if (!validateRegisterForm(form)) return@launch

            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.register(form.email.trim(), form.password, form.displayName.trim())) {
                is Resource.Success -> _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }
                is Resource.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    // ─── Forgot Password ───────────────────────────────────────────────────────

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            if (!email.isValidEmail()) {
                _uiState.update { it.copy(error = "Email invalid") }
                return@launch
            }
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.sendPasswordResetEmail(email)) {
                is Resource.Success -> _uiState.update { it.copy(
                    isLoading = false,
                    successMessage = "Email de resetare trimis la $email"
                )}
                is Resource.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    // ─── Sign Out ──────────────────────────────────────────────────────────────

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _uiState.update { AuthUiState() }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
    fun clearSuccess() = _uiState.update { it.copy(successMessage = null) }

    private fun validateLoginForm(form: LoginFormState): Boolean =
        form.email.isNotEmpty() && form.password.isNotEmpty() &&
        form.emailError == null && form.passwordError == null

    private fun validateRegisterForm(form: RegisterFormState): Boolean =
        form.displayName.isNotEmpty() && form.email.isNotEmpty() &&
        form.password.isNotEmpty() && form.confirmPassword.isNotEmpty() &&
        form.nameError == null && form.emailError == null &&
        form.passwordError == null && form.confirmPasswordError == null &&
        form.password == form.confirmPassword
}
