package com.radiowave.app.features.recommendations.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radiowave.app.core.data.local.dao.TopStationResult
import com.radiowave.app.core.domain.model.RadioStation
import com.radiowave.app.features.recommendations.domain.RecommendationsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecommendationsUiState(
    val isLoading: Boolean = false,
    val recommendations: List<RadioStation> = emptyList(),
    val topListened: List<TopStationResult> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class RecommendationsViewModel @Inject constructor(
    private val recommendationsRepository: RecommendationsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecommendationsUiState())
    val uiState: StateFlow<RecommendationsUiState> = _uiState.asStateFlow()

    init {
        loadRecommendations()
    }

    fun loadRecommendations() {
        viewModelScope.launch {
            _uiState.value = RecommendationsUiState(isLoading = true)
            val topListened = recommendationsRepository.getTopListened()
            recommendationsRepository.getRecommendations().fold(
                onSuccess = {
                    _uiState.value = RecommendationsUiState(
                        recommendations = it,
                        topListened = topListened
                    )
                },
                onFailure = {
                    _uiState.value = RecommendationsUiState(
                        error = it.message ?: "Failed to load recommendations",
                        topListened = topListened
                    )
                }
            )
        }
    }
}
