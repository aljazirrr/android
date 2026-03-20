package com.radiowave.app.features.recording.data

import android.content.Context
import android.content.Intent
import android.os.Environment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.radiowave.app.core.data.local.dao.RecordingDao
import com.radiowave.app.core.data.local.entities.RecordingEntity
import com.radiowave.app.features.recording.domain.RecordingRepository
import com.radiowave.app.service.RecordingService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordingRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val recordingDao: RecordingDao,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseStorage: FirebaseStorage
) : RecordingRepository {

    private val _isRecording = MutableStateFlow(false)
    override val isRecording: Flow<Boolean> = _isRecording

    private val _currentRecordingStation = MutableStateFlow<String?>(null)
    override val currentRecordingStation: Flow<String?> = _currentRecordingStation

    private var currentStationUuid: String? = null
    private var currentStationName: String? = null
    private var recordingStartTime: Long = 0

    override fun startRecording(stationName: String, stationUuid: String) {
        if (_isRecording.value) return

        val outputDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.absolutePath
            ?: context.filesDir.absolutePath + "/recordings"

        currentStationUuid = stationUuid
        currentStationName = stationName
        recordingStartTime = System.currentTimeMillis()

        _isRecording.value = true
        _currentRecordingStation.value = stationName

        val intent = Intent(context, RecordingService::class.java).apply {
            action = RecordingService.ACTION_START_RECORDING
            putExtra(RecordingService.EXTRA_STATION_NAME, stationName)
            putExtra(RecordingService.EXTRA_OUTPUT_DIR, outputDir)
        }
        context.startForegroundService(intent)
    }

    override fun stopRecording() {
        if (!_isRecording.value) return

        val intent = Intent(context, RecordingService::class.java).apply {
            action = RecordingService.ACTION_STOP_RECORDING
        }
        context.startService(intent)

        _isRecording.value = false
        _currentRecordingStation.value = null
    }

    suspend fun saveRecording(filePath: String, sizeBytes: Long) {
        val duration = System.currentTimeMillis() - recordingStartTime
        val stationName = currentStationName ?: "Unknown"
        val stationUuid = currentStationUuid ?: ""

        recordingDao.insertRecording(
            RecordingEntity(
                stationUuid = stationUuid,
                stationName = stationName,
                title = "${stationName} - ${java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}",
                filePath = filePath,
                durationMs = duration,
                sizeBytes = sizeBytes,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun uploadToCloud(recordingId: Long): Result<String> = runCatching {
        val userId = firebaseAuth.currentUser?.uid ?: throw Exception("Not signed in")
        // Get recording from db - we'd need a query by id here
        // For now returning a placeholder
        "cloud://uploaded"
    }
}
