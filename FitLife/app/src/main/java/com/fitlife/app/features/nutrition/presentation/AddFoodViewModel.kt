package com.fitlife.app.features.nutrition.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitlife.app.core.domain.model.Food
import com.fitlife.app.features.nutrition.domain.NutritionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddFoodUiState(
    val isLoading: Boolean = false,
    val foods: List<Food> = emptyList(),
    val searchQuery: String = ""
)

@OptIn(FlowPreview::class)
@HiltViewModel
class AddFoodViewModel @Inject constructor(
    private val nutritionRepository: NutritionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddFoodUiState(isLoading = true))
    val uiState: StateFlow<AddFoodUiState> = _uiState.asStateFlow()

    private val searchQuery = MutableStateFlow("")

    init {
        viewModelScope.launch {
            nutritionRepository.seedDefaultFoods()
        }
        viewModelScope.launch {
            searchQuery
                .debounce(300)
                .flatMapLatest { query ->
                    if (query.isEmpty()) nutritionRepository.getAllFoods()
                    else nutritionRepository.searchFoods(query)
                }
                .collect { foods ->
                    _uiState.update { it.copy(foods = foods, isLoading = false) }
                }
        }
    }

    fun onSearch(query: String) {
        searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }
}
