package com.radiowave.app.features.player.domain

import com.radiowave.app.core.domain.model.PlayerState
import com.radiowave.app.core.domain.model.RadioStation
import kotlinx.coroutines.flow.Flow

interface PlayerRepository {
    val playerState: Flow<PlayerState>
    val currentStation: Flow<RadioStation?>
    val volume: Flow<Float>
    fun playStation(station: RadioStation)
    fun pause()
    fun resume()
    fun stop()
    fun setVolume(volume: Float)
    fun toggleFavorite(stationUuid: String, isFavorite: Boolean)
}
