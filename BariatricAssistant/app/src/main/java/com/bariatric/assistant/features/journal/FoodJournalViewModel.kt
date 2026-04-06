package com.bariatric.assistant.features.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bariatric.assistant.core.data.local.entity.FoodJournalEntryEntity
import com.bariatric.assistant.core.data.local.entity.MealType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FoodJournalUiState(
    val entries: List<FoodJournalEntryEntity> = emptyList(),
    val showAddDialog: Boolean = false,
    val foodName: String = "",
    val selectedMealType: MealType = MealType.BREAKFAST,
    val portionDescription: String = "",
    val notes: String = ""
)

@HiltViewModel
class FoodJournalViewModel @Inject constructor(
    private val repository: FoodJournalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FoodJournalUiState())
    val uiState: StateFlow<FoodJournalUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getTodayEntries().collect { entries ->
                _uiState.update { it.copy(entries = entries) }
            }
        }
    }

    fun showAddDialog() {
        _uiState.update {
            it.copy(
                showAddDialog = true,
                foodName = "",
                portionDescription = "",
                notes = "",
                selectedMealType = MealType.BREAKFAST
            )
        }
    }

    fun hideAddDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }

    fun updateFoodName(name: String) {
        _uiState.update { it.copy(foodName = name) }
    }

    fun updateMealType(type: MealType) {
        _uiState.update { it.copy(selectedMealType = type) }
    }

    fun updatePortionDescription(desc: String) {
        _uiState.update { it.copy(portionDescription = desc) }
    }

    fun updateNotes(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun addEntry() {
        val state = _uiState.value
        if (state.foodName.isBlank() || state.portionDescription.isBlank()) return

        viewModelScope.launch {
            repository.addEntry(
                foodName = state.foodName.trim(),
                mealType = state.selectedMealType,
                portionDescription = state.portionDescription.trim(),
                notes = state.notes.trim().ifBlank { null }
            )
            _uiState.update { it.copy(showAddDialog = false) }
        }
    }

    fun removeEntry(entry: FoodJournalEntryEntity) {
        viewModelScope.launch {
            repository.removeEntry(entry)
        }
    }
}
