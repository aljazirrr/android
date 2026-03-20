package com.radiowave.app.features.recording.domain

import com.radiowave.app.core.domain.model.Recording
import kotlinx.coroutines.flow.Flow

interface RecordingRepository {
    val isRecording: Flow<Boolean>
    val currentRecordingStation: Flow<String?>
    fun startRecording(stationName: String, stationUuid: String)
    fun stopRecording()
    suspend fun uploadToCloud(recordingId: Long): Result<String>
}
