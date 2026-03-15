package com.fitlife.app.features.exercises.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitlife.app.core.domain.model.Exercise
import com.fitlife.app.core.utils.*
import com.fitlife.app.features.exercises.domain.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExerciseUiState(
    val isLoading: Boolean = true,
    val allExercises: List<Exercise> = emptyList(),
    val filteredExercises: List<Exercise> = emptyList(),
    val searchQuery: String = "",
    val selectedMuscleGroup: MuscleGroup? = null,
    val selectedEquipment: Equipment? = null,
    val selectedType: ExerciseType? = null,
    val selectedExercise: Exercise? = null,
    val error: String? = null
) {
    val hasActiveFilters get() = searchQuery.isNotEmpty() ||
            selectedMuscleGroup != null || selectedEquipment != null || selectedType != null
}

@HiltViewModel
class ExerciseViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExerciseUiState())
    val uiState: StateFlow<ExerciseUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            exerciseRepository.seedDefaultExercises()
        }
        loadExercises()
    }

    private fun loadExercises() {
        viewModelScope.launch {
            exerciseRepository.getAllExercises().collect { exercises ->
                _uiState.update { state ->
                    state.copy(
                        allExercises = exercises,
                        filteredExercises = applyFilters(exercises, state),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onSearchChange(query: String) {
        _uiState.update { state ->
            val filtered = applyFilters(state.allExercises, state.copy(searchQuery = query))
            state.copy(searchQuery = query, filteredExercises = filtered)
        }
    }

    fun onMuscleGroupFilter(muscle: MuscleGroup?) {
        _uiState.update { state ->
            val filtered = applyFilters(state.allExercises, state.copy(selectedMuscleGroup = muscle))
            state.copy(selectedMuscleGroup = muscle, filteredExercises = filtered)
        }
    }

    fun onEquipmentFilter(equipment: Equipment?) {
        _uiState.update { state ->
            val filtered = applyFilters(state.allExercises, state.copy(selectedEquipment = equipment))
            state.copy(selectedEquipment = equipment, filteredExercises = filtered)
        }
    }

    fun onTypeFilter(type: ExerciseType?) {
        _uiState.update { state ->
            val filtered = applyFilters(state.allExercises, state.copy(selectedType = type))
            state.copy(selectedType = type, filteredExercises = filtered)
        }
    }

    fun clearFilters() {
        _uiState.update { state ->
            state.copy(
                searchQuery = "",
                selectedMuscleGroup = null,
                selectedEquipment = null,
                selectedType = null,
                filteredExercises = state.allExercises
            )
        }
    }

    fun selectExercise(exerciseId: String) {
        viewModelScope.launch {
            val exercise = exerciseRepository.getExerciseById(exerciseId)
            _uiState.update { it.copy(selectedExercise = exercise) }
        }
    }

    private fun applyFilters(exercises: List<Exercise>, state: ExerciseUiState): List<Exercise> {
        return exercises.filter { exercise ->
            val matchesSearch = state.searchQuery.isEmpty() ||
                    exercise.name.contains(state.searchQuery, ignoreCase = true) ||
                    exercise.description.contains(state.searchQuery, ignoreCase = true)
            val matchesMuscle = state.selectedMuscleGroup == null ||
                    exercise.muscleGroups.contains(state.selectedMuscleGroup)
            val matchesEquipment = state.selectedEquipment == null ||
                    exercise.equipment == state.selectedEquipment
            val matchesType = state.selectedType == null ||
                    exercise.type == state.selectedType
            matchesSearch && matchesMuscle && matchesEquipment && matchesType
        }
    }
}
