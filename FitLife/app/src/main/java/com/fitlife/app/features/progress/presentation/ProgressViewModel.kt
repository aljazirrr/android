package com.fitlife.app.features.progress.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitlife.app.core.domain.model.*
import com.fitlife.app.features.auth.domain.repository.AuthRepository
import com.fitlife.app.features.progress.domain.ProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProgressUiState(
    val isLoading: Boolean = true,
    val measurements: List<BodyMeasurement> = emptyList(),
    val latestMeasurement: BodyMeasurement? = null,
    val photos: List<ProgressPhoto> = emptyList(),
    val personalRecords: List<PersonalRecord> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val progressRepository: ProgressRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    init {
        loadProgressData()
    }

    private fun loadProgressData() {
        val userId = authRepository.currentUser?.uid ?: return

        viewModelScope.launch {
            progressRepository.getMeasurementsByUser(userId).collect { measurements ->
                _uiState.update { it.copy(measurements = measurements, isLoading = false) }
            }
        }
        viewModelScope.launch {
            progressRepository.getLatestMeasurement(userId).collect { latest ->
                _uiState.update { it.copy(latestMeasurement = latest) }
            }
        }
        viewModelScope.launch {
            progressRepository.getPhotosByUser(userId).collect { photos ->
                _uiState.update { it.copy(photos = photos) }
            }
        }
        viewModelScope.launch {
            progressRepository.getPersonalRecords(userId).collect { records ->
                _uiState.update { it.copy(personalRecords = records) }
            }
        }
    }

    fun addMeasurement(measurement: BodyMeasurement) {
        viewModelScope.launch {
            progressRepository.addMeasurement(measurement)
        }
    }

    fun deleteMeasurement(measurementId: String) {
        viewModelScope.launch {
            progressRepository.deleteMeasurement(measurementId)
        }
    }
}
