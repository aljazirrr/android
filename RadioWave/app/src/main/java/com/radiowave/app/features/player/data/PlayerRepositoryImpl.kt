package com.radiowave.app.features.player.data

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.radiowave.app.core.data.local.dao.FavoriteStationDao
import com.radiowave.app.core.data.local.dao.ListeningHistoryDao
import com.radiowave.app.core.data.local.entities.FavoriteStationEntity
import com.radiowave.app.core.data.local.entities.ListeningHistoryEntity
import com.radiowave.app.core.domain.model.PlayerState
import com.radiowave.app.core.domain.model.RadioStation
import com.radiowave.app.features.player.domain.PlayerRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerRepositoryImpl @Inject constructor(
    private val exoPlayer: ExoPlayer,
    private val favoriteDao: FavoriteStationDao,
    private val historyDao: ListeningHistoryDao
) : PlayerRepository {

    private val _playerState = MutableStateFlow(PlayerState.IDLE)
    override val playerState: Flow<PlayerState> = _playerState.asStateFlow()

    private val _currentStation = MutableStateFlow<RadioStation?>(null)
    override val currentStation: Flow<RadioStation?> = _currentStation.asStateFlow()

    private val _volume = MutableStateFlow(1f)
    override val volume: Flow<Float> = _volume.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var playbackStartTime: Long = 0
    private var historyJob: Job? = null

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                _playerState.value = when (playbackState) {
                    Player.STATE_BUFFERING -> PlayerState.BUFFERING
                    Player.STATE_READY -> if (exoPlayer.playWhenReady) PlayerState.PLAYING else PlayerState.PAUSED
                    Player.STATE_ENDED -> PlayerState.IDLE
                    else -> PlayerState.IDLE
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    _playerState.value = PlayerState.PLAYING
                    playbackStartTime = System.currentTimeMillis()
                } else if (_playerState.value == PlayerState.PLAYING) {
                    _playerState.value = PlayerState.PAUSED
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                Timber.e(error, "ExoPlayer error")
                _playerState.value = PlayerState.ERROR
            }
        })
    }

    override fun playStation(station: RadioStation) {
        _currentStation.value?.let { prev ->
            if (playbackStartTime > 0) {
                val duration = System.currentTimeMillis() - playbackStartTime
                if (duration > 5000) saveToHistory(prev, duration)
            }
        }
        _currentStation.value = station
        _playerState.value = PlayerState.LOADING
        playbackStartTime = System.currentTimeMillis()

        val streamUrl = station.urlResolved.ifEmpty { station.url }
        exoPlayer.setMediaItem(MediaItem.fromUri(streamUrl))
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    override fun pause() {
        exoPlayer.pause()
    }

    override fun resume() {
        exoPlayer.play()
    }

    override fun stop() {
        _currentStation.value?.let { station ->
            if (playbackStartTime > 0) {
                val duration = System.currentTimeMillis() - playbackStartTime
                if (duration > 5000) saveToHistory(station, duration)
            }
        }
        exoPlayer.stop()
        _playerState.value = PlayerState.IDLE
        _currentStation.value = null
        playbackStartTime = 0
    }

    override fun setVolume(volume: Float) {
        _volume.value = volume.coerceIn(0f, 1f)
        exoPlayer.volume = _volume.value
    }

    override fun toggleFavorite(stationUuid: String, isFavorite: Boolean) {
        scope.launch {
            if (isFavorite) {
                favoriteDao.deleteFavorite(stationUuid)
            } else {
                val station = _currentStation.value ?: return@launch
                favoriteDao.insertFavorite(
                    FavoriteStationEntity(
                        stationUuid = station.stationUuid,
                        name = station.name,
                        url = station.url,
                        urlResolved = station.urlResolved,
                        homepage = station.homepage,
                        favicon = station.favicon,
                        country = station.country,
                        countryCode = station.countryCode,
                        language = station.language,
                        tags = station.tags.joinToString(","),
                        votes = station.votes,
                        codec = station.codec,
                        bitrate = station.bitrate
                    )
                )
            }
        }
    }

    private fun saveToHistory(station: RadioStation, durationMs: Long) {
        historyJob?.cancel()
        historyJob = scope.launch {
            historyDao.insertHistory(
                ListeningHistoryEntity(
                    stationUuid = station.stationUuid,
                    stationName = station.name,
                    stationFavicon = station.favicon,
                    listenedAt = System.currentTimeMillis(),
                    durationMs = durationMs
                )
            )
        }
    }
}
