package com.radiowave.app.core.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "favorite_stations")
data class FavoriteStationEntity(
    @PrimaryKey val stationUuid: String,
    val name: String,
    val url: String,
    val urlResolved: String,
    val homepage: String,
    val favicon: String,
    val country: String,
    val countryCode: String,
    val language: String,
    val tags: String,
    val votes: Int,
    val codec: String,
    val bitrate: Int,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "recordings")
data class RecordingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val stationUuid: String,
    val stationName: String,
    val title: String,
    val filePath: String,
    val cloudUrl: String? = null,
    val durationMs: Long,
    val sizeBytes: Long,
    val createdAt: Long,
    val isUploaded: Boolean = false
)

@Entity(tableName = "scheduled_recordings")
data class ScheduledRecordingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val stationUuid: String,
    val stationName: String,
    val stationUrl: String,
    val title: String,
    val scheduledAt: Long,
    val durationMinutes: Int,
    val isEnabled: Boolean = true,
    val workerId: String? = null
)

@Entity(tableName = "listening_history")
data class ListeningHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val stationUuid: String,
    val stationName: String,
    val stationFavicon: String,
    val listenedAt: Long,
    val durationMs: Long
)
