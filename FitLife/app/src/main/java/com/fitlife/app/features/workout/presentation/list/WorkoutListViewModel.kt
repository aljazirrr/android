package com.fitlife.app.features.workout.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitlife.app.core.domain.model.WorkoutSession
import com.fitlife.app.core.domain.model.WorkoutStreak
import com.fitlife.app.features.auth.domain.repository.AuthRepository
import com.fitlife.app.features.workout.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkoutListUiState(
    val isLoading: Boolean = true,
    val sessions: List<WorkoutSession> = emptyList(),
    val streak: WorkoutStreak = WorkoutStreak(),
    val error: String? = null
)

@HiltViewModel
class WorkoutListViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutListUiState())
    val uiState: StateFlow<WorkoutListUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val userId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            combine(
                workoutRepository.getCompletedSessions(userId),
                workoutRepository.getWorkoutStreak(userId)
            ) { sessions, streak ->
                WorkoutListUiState(
                    isLoading = false,
                    sessions = sessions.sortedByDescending { it.completedAt ?: it.scheduledDate },
                    streak = streak
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}
