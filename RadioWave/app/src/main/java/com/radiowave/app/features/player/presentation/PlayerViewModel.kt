package com.radiowave.app.features.player.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radiowave.app.core.domain.model.PlayerState
import com.radiowave.app.core.domain.model.PlayerUiState
import com.radiowave.app.core.domain.model.RadioStation
import com.radiowave.app.features.player.domain.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerRepository: PlayerRepository
) : ViewModel() {

    val playerUiState: StateFlow<PlayerUiState> = combine(
        playerRepository.currentStation,
        playerRepository.playerState,
        playerRepository.volume
    ) { station, state, volume ->
        PlayerUiState(
            currentStation = station,
            playerState = state,
            volume = volume
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        PlayerUiState()
    )

    fun playStation(station: RadioStation) {
        playerRepository.playStation(station)
    }

    fun togglePlayPause() {
        val state = playerUiState.value.playerState
        when (state) {
            PlayerState.PLAYING -> playerRepository.pause()
            PlayerState.PAUSED -> playerRepository.resume()
            else -> {}
        }
    }

    fun stop() {
        playerRepository.stop()
    }

    fun setVolume(volume: Float) {
        playerRepository.setVolume(volume)
    }

    fun toggleFavorite() {
        val station = playerUiState.value.currentStation ?: return
        playerRepository.toggleFavorite(station.stationUuid, station.isFavorite)
    }
}
