package com.radiowave.app.core.domain.model

import java.util.Date

data class RadioStation(
    val stationUuid: String,
    val name: String,
    val url: String,
    val urlResolved: String,
    val homepage: String,
    val favicon: String,
    val country: String,
    val countryCode: String,
    val language: String,
    val tags: List<String>,
    val votes: Int,
    val codec: String,
    val bitrate: Int,
    val clickCount: Int,
    val clickTrend: Int,
    val isFavorite: Boolean = false
)

data class Recording(
    val id: Long = 0,
    val stationUuid: String,
    val stationName: String,
    val title: String,
    val filePath: String,
    val cloudUrl: String? = null,
    val durationMs: Long,
    val sizeBytes: Long,
    val createdAt: Date,
    val isUploaded: Boolean = false
)

data class ScheduledRecording(
    val id: Long = 0,
    val stationUuid: String,
    val stationName: String,
    val stationUrl: String,
    val title: String,
    val scheduledAt: Date,
    val durationMinutes: Int,
    val isEnabled: Boolean = true,
    val workerId: String? = null
)

data class ListeningHistory(
    val id: Long = 0,
    val stationUuid: String,
    val stationName: String,
    val stationFavicon: String,
    val listenedAt: Date,
    val durationMs: Long
)

data class UserProfile(
    val uid: String,
    val email: String,
    val displayName: String,
    val photoUrl: String?,
    val favoriteGenres: List<String> = emptyList(),
    val favoriteCountries: List<String> = emptyList()
)

enum class PlayerState {
    IDLE, LOADING, PLAYING, PAUSED, ERROR, BUFFERING
}

data class PlayerUiState(
    val currentStation: RadioStation? = null,
    val playerState: PlayerState = PlayerState.IDLE,
    val volume: Float = 1f,
    val errorMessage: String? = null
)

sealed class SearchFilter {
    data class ByName(val query: String) : SearchFilter()
    data class ByTag(val tag: String) : SearchFilter()
    data class ByCountry(val country: String) : SearchFilter()
    data class ByLanguage(val language: String) : SearchFilter()
    object ByPopularity : SearchFilter()
    object ByTrending : SearchFilter()
}
