package com.radiowave.app.features.search.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radiowave.app.core.domain.model.RadioStation
import com.radiowave.app.core.domain.model.SearchFilter
import com.radiowave.app.features.search.domain.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class SearchUiState(
    val isLoading: Boolean = false,
    val results: List<RadioStation> = emptyList(),
    val query: String = "",
    val activeFilter: FilterType = FilterType.NAME,
    val error: String? = null,
    val hasMore: Boolean = true
)

enum class FilterType { NAME, TAG, COUNTRY, LANGUAGE, POPULAR, TRENDING }

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var currentOffset = 0

    fun onQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        if (query.length >= 2 || _uiState.value.activeFilter != FilterType.NAME) {
            debounceSearch()
        }
    }

    fun setFilter(filter: FilterType) {
        _uiState.value = _uiState.value.copy(activeFilter = filter)
        search()
    }

    fun search(loadMore: Boolean = false) {
        searchJob?.cancel()
        if (!loadMore) currentOffset = 0

        val state = _uiState.value
        if (state.query.isEmpty() && state.activeFilter == FilterType.NAME) return

        searchJob = viewModelScope.launch {
            if (!loadMore) {
                _uiState.value = state.copy(isLoading = true, results = emptyList())
            }

            val filter = when (state.activeFilter) {
                FilterType.NAME -> SearchFilter.ByName(state.query)
                FilterType.TAG -> SearchFilter.ByTag(state.query)
                FilterType.COUNTRY -> SearchFilter.ByCountry(state.query)
                FilterType.LANGUAGE -> SearchFilter.ByLanguage(state.query)
                FilterType.POPULAR -> SearchFilter.ByPopularity
                FilterType.TRENDING -> SearchFilter.ByTrending
            }

            searchRepository.search(filter, currentOffset).fold(
                onSuccess = { results ->
                    currentOffset += results.size
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        results = if (loadMore) _uiState.value.results + results else results,
                        hasMore = results.size >= 50,
                        error = null
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = it.message ?: "Search failed"
                    )
                }
            )
        }
    }

    fun loadMore() {
        if (!_uiState.value.isLoading && _uiState.value.hasMore) {
            search(loadMore = true)
        }
    }

    private fun debounceSearch() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            search()
        }
    }
}
