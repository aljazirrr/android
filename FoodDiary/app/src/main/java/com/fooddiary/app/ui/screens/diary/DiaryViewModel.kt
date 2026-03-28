package com.fooddiary.app.ui.screens.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fooddiary.app.data.repository.FoodDiaryRepository
import com.fooddiary.app.domain.model.DailyLog
import com.fooddiary.app.domain.model.FoodEntry
import com.fooddiary.app.domain.model.MealType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class DiaryUiState(
    val isLoading: Boolean = true,
    val selectedDate: Long = startOfDay(System.currentTimeMillis()),
    val entries: List<FoodEntry> = emptyList(),
    val dailyLog: DailyLog? = null,
    val totalCalories: Int = 0,
    val totalProteinG: Float = 0f,
    val totalCarbsG: Float = 0f,
    val totalFatG: Float = 0f,
    val totalFiberG: Float = 0f,
    val totalSugarG: Float = 0f,
    val waterMl: Int = 0
)

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val repository: FoodDiaryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiaryUiState())
    val uiState: StateFlow<DiaryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { repository.seedDefaultFoods() }
        loadDate(_uiState.value.selectedDate)
    }

    fun selectDate(date: Long) {
        val normalized = startOfDay(date)
        _uiState.update { it.copy(selectedDate = normalized) }
        loadDate(normalized)
    }

    fun previousDay() = selectDate(_uiState.value.selectedDate - 86400000L)
    fun nextDay() {
        val next = _uiState.value.selectedDate + 86400000L
        if (next <= startOfDay(System.currentTimeMillis())) selectDate(next)
    }

    fun addWater(amountMl: Int = 250) {
        viewModelScope.launch {
            repository.addWater(_uiState.value.selectedDate, amountMl)
        }
    }

    fun removeEntry(entryId: String) {
        viewModelScope.launch {
            repository.removeFoodEntry(entryId)
        }
    }

    fun updateNotes(notes: String) {
        viewModelScope.launch {
            repository.updateNotes(_uiState.value.selectedDate, notes)
        }
    }

    private fun loadDate(date: Long) {
        viewModelScope.launch {
            combine(
                repository.getEntriesForDate(date),
                repository.getDailyLog(date)
            ) { entries, log ->
                DiaryUiState(
                    isLoading = false,
                    selectedDate = date,
                    entries = entries,
                    dailyLog = log,
                    totalCalories = entries.sumOf { it.calories },
                    totalProteinG = entries.map { it.proteinG }.sum(),
                    totalCarbsG = entries.map { it.carbsG }.sum(),
                    totalFatG = entries.map { it.fatG }.sum(),
                    totalFiberG = entries.map { it.fiberG }.sum(),
                    totalSugarG = entries.map { it.sugarG }.sum(),
                    waterMl = log?.waterMl ?: 0
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}

fun startOfDay(millis: Long): Long {
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

fun startOfWeek(millis: Long): Long {
    val cal = Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
    }
    return startOfDay(cal.timeInMillis)
}

fun Long.toDateString(pattern: String = "d MMM yyyy"): String {
    val sdf = java.text.SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(Date(this))
}

fun Long.isToday(): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = this@isToday }
    val cal2 = Calendar.getInstance()
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
