package com.radiowave.app.features.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radiowave.app.core.data.remote.CountryDto
import com.radiowave.app.core.data.remote.TagDto
import com.radiowave.app.core.domain.model.RadioStation
import com.radiowave.app.features.home.domain.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val topStations: List<RadioStation> = emptyList(),
    val trendingStations: List<RadioStation> = emptyList(),
    val recentStations: List<RadioStation> = emptyList(),
    val topTags: List<TagDto> = emptyList(),
    val topCountries: List<CountryDto> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = HomeUiState(isLoading = true)
            try {
                val topDeferred = async { homeRepository.getTopStations() }
                val trendingDeferred = async { homeRepository.getTrendingStations() }
                val recentDeferred = async { homeRepository.getRecentStations() }
                val tagsDeferred = async { homeRepository.getTopTags() }
                val countriesDeferred = async { homeRepository.getTopCountries() }

                _uiState.value = HomeUiState(
                    isLoading = false,
                    topStations = topDeferred.await().getOrDefault(emptyList()),
                    trendingStations = trendingDeferred.await().getOrDefault(emptyList()),
                    recentStations = recentDeferred.await().getOrDefault(emptyList()),
                    topTags = tagsDeferred.await().getOrDefault(emptyList()),
                    topCountries = countriesDeferred.await().getOrDefault(emptyList())
                )
            } catch (e: Exception) {
                _uiState.value = HomeUiState(error = e.message ?: "Failed to load data")
            }
        }
    }
}
