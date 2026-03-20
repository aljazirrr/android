package com.radiowave.app.features.favorites.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radiowave.app.core.domain.model.RadioStation
import com.radiowave.app.features.favorites.domain.FavoritesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    val favorites: StateFlow<List<RadioStation>> = favoritesRepository.getFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun removeFavorite(station: RadioStation) {
        viewModelScope.launch {
            favoritesRepository.removeFavorite(station.stationUuid)
        }
    }

    fun addFavorite(station: RadioStation) {
        viewModelScope.launch {
            favoritesRepository.addFavorite(station)
        }
    }
}
