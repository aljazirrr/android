package com.fitlife.app.features.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitlife.app.core.domain.model.*
import com.fitlife.app.core.utils.*
import com.fitlife.app.features.auth.domain.repository.AuthRepository
import com.fitlife.app.features.progress.domain.ProgressRepository
import com.fitlife.app.features.workout.domain.repository.WorkoutRepository
import com.fitlife.app.features.nutrition.domain.NutritionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val todayStats: DailyStats? = null,
    val streak: WorkoutStreak = WorkoutStreak(),
    val recentWorkouts: List<WorkoutSession> = emptyList(),
    val todayNutrition: NutritionLog? = null,
    val latestMeasurement: BodyMeasurement? = null,
    val weeklyWorkouts: List<WorkoutSession> = emptyList(),
    val error: String? = null,
    val motivationalQuote: MotivationalQuote = MotivationalQuote.random()
)

data class MotivationalQuote(val text: String, val author: String) {
    companion object {
        private val quotes = listOf(
            MotivationalQuote("Fiecare antrenament este un pas spre versiunea mai bună a ta.", "FitLife"),
            MotivationalQuote("Durerea este temporară. Mândria durează pentru totdeauna.", "Muhammad Ali"),
            MotivationalQuote("Nu opri când ești obosit. Oprește când ești gata.", "FitLife"),
            MotivationalQuote("Succesul nu vine la tine. Tu mergi la el.", "Marva Collins"),
            MotivationalQuote("Fiecare repetiție contează. Fiecare zi contează.", "FitLife"),
            MotivationalQuote("Corpul tău poate face. Mintea ta decide.", "FitLife"),
            MotivationalQuote("Nu există scurtături spre orice loc care merită să mergi.", "Beverly Sills")
        )
        fun random() = quotes.random()
    }
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val workoutRepository: WorkoutRepository,
    private val progressRepository: ProgressRepository,
    private val nutritionRepository: NutritionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            authRepository.getCurrentUserFlow()
                .filterNotNull()
                .collect { user ->
                    _uiState.update { it.copy(user = user, isLoading = false) }
                    loadUserData(user.uid)
                }
        }
    }

    private fun loadUserData(userId: String) {
        val today = System.currentTimeMillis()
        val cal = Calendar.getInstance()

        // Load streak
        viewModelScope.launch {
            workoutRepository.getWorkoutStreak(userId).collect { streak ->
                _uiState.update { it.copy(streak = streak) }
            }
        }

        // Load recent workouts
        viewModelScope.launch {
            workoutRepository.getRecentSessions(userId, limit = 5).collect { sessions ->
                _uiState.update { it.copy(recentWorkouts = sessions) }
            }
        }

        // Load today's nutrition
        viewModelScope.launch {
            nutritionRepository.getNutritionLogForDate(userId, today.startOfDay()).collect { log ->
                _uiState.update { it.copy(todayNutrition = log) }
            }
        }

        // Load today's stats
        viewModelScope.launch {
            progressRepository.getStatsForDate(userId, today.startOfDay()).collect { stats ->
                _uiState.update { it.copy(todayStats = stats) }
            }
        }

        // Load latest body measurement
        viewModelScope.launch {
            progressRepository.getLatestMeasurement(userId).collect { measurement ->
                _uiState.update { it.copy(latestMeasurement = measurement) }
            }
        }

        // Load weekly workouts
        viewModelScope.launch {
            val weekStart = today.startOfWeek()
            val weekEnd = today.endOfWeek()
            workoutRepository.getSessionsInRange(userId, weekStart, weekEnd).collect { sessions ->
                _uiState.update { it.copy(weeklyWorkouts = sessions) }
            }
        }
    }

    fun refresh() {
        _uiState.update { it.copy(motivationalQuote = MotivationalQuote.random()) }
        val userId = _uiState.value.user?.uid ?: return
        loadUserData(userId)
    }
}
