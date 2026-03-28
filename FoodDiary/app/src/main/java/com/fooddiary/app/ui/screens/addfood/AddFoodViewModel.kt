package com.fooddiary.app.ui.screens.addfood

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fooddiary.app.data.repository.FoodDiaryRepository
import com.fooddiary.app.domain.model.Food
import com.fooddiary.app.domain.model.MealType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddFoodUiState(
    val foods: List<Food> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true
)

@OptIn(FlowPreview::class)
@HiltViewModel
class AddFoodViewModel @Inject constructor(
    private val repository: FoodDiaryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddFoodUiState())
    val uiState: StateFlow<AddFoodUiState> = _uiState.asStateFlow()

    private val searchQuery = MutableStateFlow("")

    init {
        viewModelScope.launch {
            searchQuery
                .debounce(300)
                .flatMapLatest { query ->
                    if (query.isBlank()) repository.getAllFoods()
                    else repository.searchFoods(query)
                }
                .collect { foods ->
                    _uiState.update { it.copy(foods = foods, isLoading = false) }
                }
        }
    }

    fun onSearch(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchQuery.value = query
    }

    fun addFood(date: Long, mealType: MealType, food: Food, quantityGrams: Float) {
        viewModelScope.launch {
            repository.addFoodEntry(date, mealType, food, quantityGrams)
        }
    }
}
