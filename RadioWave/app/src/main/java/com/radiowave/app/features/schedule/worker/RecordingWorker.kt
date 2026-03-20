package com.radiowave.app.features.schedule.worker

import android.content.Context
import android.content.Intent
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.radiowave.app.service.RecordingService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import timber.log.Timber
import java.util.concurrent.TimeUnit

@HiltWorker
class RecordingWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val stationName = inputData.getString(KEY_STATION_NAME) ?: return Result.failure()
        val stationUrl = inputData.getString(KEY_STATION_URL) ?: return Result.failure()
        val durationMinutes = inputData.getInt(KEY_DURATION_MINUTES, 30)

        Timber.d("Starting scheduled recording: $stationName for $durationMinutes minutes")

        // Start recording service
        val startIntent = Intent(context, RecordingService::class.java).apply {
            action = RecordingService.ACTION_START_RECORDING
            putExtra(RecordingService.EXTRA_STATION_NAME, stationName)
            putExtra(RecordingService.EXTRA_OUTPUT_DIR, context.filesDir.absolutePath + "/recordings")
        }
        context.startForegroundService(startIntent)

        // Wait for the duration
        delay(TimeUnit.MINUTES.toMillis(durationMinutes.toLong()))

        // Stop recording
        val stopIntent = Intent(context, RecordingService::class.java).apply {
            action = RecordingService.ACTION_STOP_RECORDING
        }
        context.startService(stopIntent)

        return Result.success()
    }

    companion object {
        const val KEY_STATION_NAME = "station_name"
        const val KEY_STATION_URL = "station_url"
        const val KEY_DURATION_MINUTES = "duration_minutes"
        const val WORK_TAG_PREFIX = "scheduled_recording_"

        fun buildWorkRequest(
            stationName: String,
            stationUrl: String,
            durationMinutes: Int,
            delayMs: Long
        ): OneTimeWorkRequest {
            val inputData = workDataOf(
                KEY_STATION_NAME to stationName,
                KEY_STATION_URL to stationUrl,
                KEY_DURATION_MINUTES to durationMinutes
            )
            return OneTimeWorkRequestBuilder<RecordingWorker>()
                .setInputData(inputData)
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .addTag(WORK_TAG_PREFIX + stationName)
                .build()
        }
    }
}
