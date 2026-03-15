package com.fitlife.app.features.nutrition.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitlife.app.core.domain.model.FoodEntry
import com.fitlife.app.core.domain.model.NutritionLog
import com.fitlife.app.core.utils.MealType
import com.fitlife.app.core.utils.startOfDay
import com.fitlife.app.features.auth.domain.repository.AuthRepository
import com.fitlife.app.features.nutrition.domain.NutritionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NutritionUiState(
    val isLoading: Boolean = true,
    val todayLog: NutritionLog? = null,
    val calorieTarget: Int = 2000,
    val proteinTarget: Int = 150,
    val carbsTarget: Int = 250,
    val fatTarget: Int = 65,
    val waterTarget: Int = 2500,
    val caloriesBurned: Int = 0,
    val error: String? = null
)

@HiltViewModel
class NutritionViewModel @Inject constructor(
    private val nutritionRepository: NutritionRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NutritionUiState())
    val uiState: StateFlow<NutritionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { nutritionRepository.seedDefaultFoods() }
        loadTodayData()
    }

    private fun loadTodayData() {
        val userId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            nutritionRepository.getNutritionLogForDate(userId, System.currentTimeMillis().startOfDay())
                .collect { log ->
                    _uiState.update { it.copy(todayLog = log, isLoading = false) }
                }
        }
    }

    fun loadForDate(date: Long) {
        val userId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            nutritionRepository.getNutritionLogForDate(userId, date.startOfDay())
                .collect { log ->
                    _uiState.update { it.copy(todayLog = log) }
                }
        }
    }

    fun logWater(amountMl: Int) {
        val userId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            nutritionRepository.logWater(userId, amountMl)
        }
    }

    fun addFoodEntry(mealType: MealType, entry: FoodEntry) {
        val userId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            nutritionRepository.addFoodEntry(userId, System.currentTimeMillis(), mealType, entry)
        }
    }

    fun removeFoodEntry(mealId: String, entryId: String) {
        val userId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            nutritionRepository.removeFoodEntry(userId, System.currentTimeMillis(), mealId, entryId)
        }
    }
}
