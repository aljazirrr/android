package com.radiowave.app.features.schedule.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radiowave.app.core.domain.model.ScheduledRecording
import com.radiowave.app.features.schedule.domain.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class ScheduleUiState(
    val schedules: List<ScheduledRecording> = emptyList(),
    val showAddDialog: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            scheduleRepository.getScheduledRecordings().collect { schedules ->
                _uiState.value = _uiState.value.copy(schedules = schedules)
            }
        }
    }

    fun addSchedule(
        stationUuid: String,
        stationName: String,
        stationUrl: String,
        scheduledAt: Date,
        durationMinutes: Int
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                scheduleRepository.scheduleRecording(
                    ScheduledRecording(
                        stationUuid = stationUuid,
                        stationName = stationName,
                        stationUrl = stationUrl,
                        title = "$stationName recording",
                        scheduledAt = scheduledAt,
                        durationMinutes = durationMinutes
                    )
                )
                _uiState.value = _uiState.value.copy(isLoading = false, showAddDialog = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun cancelSchedule(schedule: ScheduledRecording) {
        viewModelScope.launch {
            scheduleRepository.cancelSchedule(schedule.id)
        }
    }

    fun toggleEnabled(id: Long, enabled: Boolean) {
        viewModelScope.launch {
            scheduleRepository.setEnabled(id, enabled)
        }
    }

    fun showAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = true)
    }

    fun dismissAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = false)
    }
}
