package com.radiowave.app.features.recording.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radiowave.app.core.domain.model.RadioStation
import com.radiowave.app.features.recording.domain.RecordingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordingViewModel @Inject constructor(
    private val recordingRepository: RecordingRepository
) : ViewModel() {

    val isRecording: StateFlow<Boolean> = recordingRepository.isRecording
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val currentRecordingStation: StateFlow<String?> = recordingRepository.currentRecordingStation
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun startRecording(station: RadioStation) {
        recordingRepository.startRecording(station.name, station.stationUuid)
    }

    fun stopRecording() {
        recordingRepository.stopRecording()
    }

    fun toggleRecording(station: RadioStation) {
        if (isRecording.value) stopRecording()
        else startRecording(station)
    }
}
