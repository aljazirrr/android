package com.bariatric.assistant.features.hydration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bariatric.assistant.core.data.local.dao.DailyHydrationTotal
import com.bariatric.assistant.core.data.local.entity.HydrationEntryEntity
import com.bariatric.assistant.core.data.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HydrationUiState(
    val todayTotalMl: Int = 0,
    val goalMl: Int = 1500,
    val todayEntries: List<HydrationEntryEntity> = emptyList(),
    val weeklyTotals: List<DailyHydrationTotal> = emptyList(),
    val progress: Float = 0f
)

@HiltViewModel
class HydrationViewModel @Inject constructor(
    private val repository: HydrationRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(HydrationUiState())
    val uiState: StateFlow<HydrationUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.getTodayTotal(),
                repository.getTodayEntries(),
                repository.getWeeklyTotals(),
                userPreferences.userProfile
            ) { total, entries, weekly, profile ->
                val goal = profile.dailyWaterGoalMl
                HydrationUiState(
                    todayTotalMl = total,
                    goalMl = goal,
                    todayEntries = entries,
                    weeklyTotals = weekly,
                    progress = (total.toFloat() / goal).coerceIn(0f, 1f)
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            repository.addWater(amountMl)
        }
    }

    fun removeEntry(entry: HydrationEntryEntity) {
        viewModelScope.launch {
            repository.removeEntry(entry)
        }
    }
}
