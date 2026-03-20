package com.radiowave.app.features.favorites.domain

import com.radiowave.app.core.domain.model.RadioStation
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    fun getFavorites(): Flow<List<RadioStation>>
    suspend fun addFavorite(station: RadioStation)
    suspend fun removeFavorite(stationUuid: String)
    suspend fun isFavorite(stationUuid: String): Boolean
}
