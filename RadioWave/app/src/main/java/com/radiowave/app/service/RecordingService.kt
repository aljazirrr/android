package com.radiowave.app.service

import android.app.*
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.radiowave.app.MainActivity
import com.radiowave.app.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class RecordingService : Service() {

    private var mediaRecorder: MediaRecorder? = null
    private var recordingFile: File? = null
    private var recordingJob: Job? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    var onRecordingStopped: ((String, Long) -> Unit)? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_RECORDING -> {
                val stationName = intent.getStringExtra(EXTRA_STATION_NAME) ?: "Unknown"
                val outputDir = intent.getStringExtra(EXTRA_OUTPUT_DIR) ?: filesDir.absolutePath
                startRecording(stationName, outputDir)
            }
            ACTION_STOP_RECORDING -> stopRecording()
        }
        return START_STICKY
    }

    private fun startRecording(stationName: String, outputDir: String) {
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification(stationName))

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "${stationName.replace(" ", "_")}_$timestamp.m4a"
        val dir = File(outputDir).apply { mkdirs() }
        recordingFile = File(dir, fileName)

        try {
            mediaRecorder = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(this)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }).apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(recordingFile!!.absolutePath)
                prepare()
                start()
            }
            Timber.d("Recording started: ${recordingFile!!.absolutePath}")
        } catch (e: Exception) {
            Timber.e(e, "Failed to start recording")
            stopSelf()
        }
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            val file = recordingFile
            if (file != null && file.exists()) {
                onRecordingStopped?.invoke(file.absolutePath, file.length())
                Timber.d("Recording stopped: ${file.absolutePath}, size: ${file.length()}")
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to stop recording")
        } finally {
            recordingFile = null
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        mediaRecorder?.release()
        mediaRecorder = null
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Radio Recording",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Active recording notification"
                setShowBadge(true)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(stationName: String): Notification {
        val stopIntent = Intent(this, RecordingService::class.java).apply {
            action = ACTION_STOP_RECORDING
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val mainIntent = Intent(this, MainActivity::class.java)
        val mainPendingIntent = PendingIntent.getActivity(
            this, 0, mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("Recording: $stationName")
            .setContentText("Tap to stop recording")
            .setContentIntent(mainPendingIntent)
            .addAction(android.R.drawable.ic_media_pause, "Stop", stopPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    companion object {
        const val CHANNEL_ID = "recording_channel"
        const val NOTIFICATION_ID = 1002
        const val ACTION_START_RECORDING = "com.radiowave.app.START_RECORDING"
        const val ACTION_STOP_RECORDING = "com.radiowave.app.STOP_RECORDING"
        const val EXTRA_STATION_NAME = "station_name"
        const val EXTRA_OUTPUT_DIR = "output_dir"
    }
}
