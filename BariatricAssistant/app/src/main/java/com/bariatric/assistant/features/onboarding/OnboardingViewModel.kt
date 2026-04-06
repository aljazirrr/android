package com.bariatric.assistant.features.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bariatric.assistant.core.data.preferences.PostOpStage
import com.bariatric.assistant.core.data.preferences.SurgeryType
import com.bariatric.assistant.core.data.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val currentStep: Int = 0,
    val userName: String = "",
    val selectedSurgeryType: SurgeryType? = null,
    val selectedPostOpStage: PostOpStage? = null,
    val isCompleting: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun updateUserName(name: String) {
        _uiState.update { it.copy(userName = name) }
    }

    fun selectSurgeryType(type: SurgeryType) {
        _uiState.update { it.copy(selectedSurgeryType = type) }
    }

    fun selectPostOpStage(stage: PostOpStage) {
        _uiState.update { it.copy(selectedPostOpStage = stage) }
    }

    fun nextStep() {
        _uiState.update { it.copy(currentStep = it.currentStep + 1) }
    }

    fun previousStep() {
        _uiState.update { it.copy(currentStep = (it.currentStep - 1).coerceAtLeast(0)) }
    }

    fun canProceed(): Boolean {
        val state = _uiState.value
        return when (state.currentStep) {
            0 -> state.userName.isNotBlank()
            1 -> state.selectedSurgeryType != null
            2 -> state.selectedPostOpStage != null
            else -> false
        }
    }

    fun completeOnboarding(onComplete: () -> Unit) {
        val state = _uiState.value
        _uiState.update { it.copy(isCompleting = true) }

        viewModelScope.launch {
            userPreferences.saveUserName(state.userName.trim())
            state.selectedSurgeryType?.let { userPreferences.saveSurgeryType(it) }
            state.selectedPostOpStage?.let { userPreferences.savePostOpStage(it) }
            userPreferences.completeOnboarding()
            onComplete()
        }
    }
}
