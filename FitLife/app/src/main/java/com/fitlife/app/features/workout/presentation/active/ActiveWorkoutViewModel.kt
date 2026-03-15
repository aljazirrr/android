package com.fitlife.app.features.workout.presentation.active

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitlife.app.core.domain.model.*
import com.fitlife.app.core.utils.*
import com.fitlife.app.features.auth.domain.repository.AuthRepository
import com.fitlife.app.features.workout.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class ActiveWorkoutUiState(
    val session: WorkoutSession? = null,
    val elapsedSeconds: Int = 0,
    val isRunning: Boolean = false,
    val currentExerciseIndex: Int = 0,
    val restTimerSeconds: Int = 0,
    val isResting: Boolean = false,
    val showFinishDialog: Boolean = false,
    val isFinished: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ActiveWorkoutViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActiveWorkoutUiState())
    val uiState: StateFlow<ActiveWorkoutUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var restTimerJob: Job? = null

    fun startWorkout(sessionName: String, exercises: List<WorkoutExercise> = emptyList()) {
        val userId = authRepository.currentUser?.uid ?: return
        val session = WorkoutSession(
            id = generateUid(),
            userId = userId,
            name = sessionName,
            status = WorkoutStatus.IN_PROGRESS,
            startTime = System.currentTimeMillis(),
            scheduledDate = System.currentTimeMillis(),
            exercises = exercises.map { we ->
                CompletedExercise(
                    id = generateUid(),
                    exerciseId = we.exerciseId,
                    exerciseName = we.exerciseName,
                    sets = List(we.sets) { i -> CompletedSet(setNumber = i + 1, reps = we.reps, weightKg = we.weightKg) }
                )
            }
        )
        viewModelScope.launch {
            workoutRepository.createSession(session)
            _uiState.update { it.copy(session = session, isRunning = true) }
            startTimer()
        }
    }

    fun loadSession(sessionId: String) {
        viewModelScope.launch {
            workoutRepository.getActiveSession(authRepository.currentUser?.uid ?: return@launch)
                .filterNotNull()
                .first()
                .let { session ->
                    _uiState.update { it.copy(session = session, isRunning = true) }
                    startTimer()
                }
        }
    }

    fun toggleTimer() {
        if (_uiState.value.isRunning) {
            pauseTimer()
        } else {
            resumeTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
            }
        }
    }

    private fun pauseTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(isRunning = false) }
    }

    private fun resumeTimer() {
        _uiState.update { it.copy(isRunning = true) }
        startTimer()
    }

    fun completeSet(exerciseIndex: Int, setIndex: Int, reps: Int?, weightKg: Float?, durationSeconds: Int? = null) {
        val session = _uiState.value.session ?: return
        val updatedExercises = session.exercises.toMutableList()
        val exercise = updatedExercises[exerciseIndex]
        val updatedSets = exercise.sets.toMutableList()
        updatedSets[setIndex] = updatedSets[setIndex].copy(
            reps = reps,
            weightKg = weightKg,
            durationSeconds = durationSeconds,
            isCompleted = true
        )
        updatedExercises[exerciseIndex] = exercise.copy(sets = updatedSets)
        val updatedSession = session.copy(exercises = updatedExercises)
        _uiState.update { it.copy(session = updatedSession) }

        viewModelScope.launch {
            workoutRepository.updateSession(updatedSession)
        }
    }

    fun startRestTimer(seconds: Int = 60) {
        restTimerJob?.cancel()
        _uiState.update { it.copy(restTimerSeconds = seconds, isResting = true) }
        restTimerJob = viewModelScope.launch {
            var remaining = seconds
            while (remaining > 0 && isActive) {
                delay(1000)
                remaining--
                _uiState.update { it.copy(restTimerSeconds = remaining) }
            }
            _uiState.update { it.copy(isResting = false, restTimerSeconds = 0) }
        }
    }

    fun skipRest() {
        restTimerJob?.cancel()
        _uiState.update { it.copy(isResting = false, restTimerSeconds = 0) }
    }

    fun showFinishDialog() = _uiState.update { it.copy(showFinishDialog = true) }
    fun hideFinishDialog() = _uiState.update { it.copy(showFinishDialog = false) }

    fun finishWorkout(rating: Int = 0, notes: String = "") {
        val session = _uiState.value.session ?: return
        timerJob?.cancel()
        restTimerJob?.cancel()

        val totalVolume = session.exercises.sumOf { exercise ->
            exercise.sets.filter { it.isCompleted }.sumOf { set ->
                ((set.reps ?: 0) * (set.weightKg ?: 0f)).toDouble()
            }
        }.toFloat()

        val caloriesBurned = estimateCalories(_uiState.value.elapsedSeconds)

        val completedSession = session.copy(
            status = WorkoutStatus.COMPLETED,
            endTime = System.currentTimeMillis(),
            durationSeconds = _uiState.value.elapsedSeconds,
            totalVolume = totalVolume,
            caloriesBurned = caloriesBurned,
            rating = rating,
            notes = notes,
            completedAt = System.currentTimeMillis()
        )

        viewModelScope.launch {
            workoutRepository.updateSession(completedSession)
            _uiState.update { it.copy(
                session = completedSession,
                isRunning = false,
                isFinished = true,
                showFinishDialog = false
            )}
        }
    }

    fun addExercise(exercise: Exercise) {
        val session = _uiState.value.session ?: return
        val completedExercise = CompletedExercise(
            id = generateUid(),
            exerciseId = exercise.id,
            exerciseName = exercise.name,
            sets = List(3) { i -> CompletedSet(setNumber = i + 1) },
            order = session.exercises.size
        )
        val updatedSession = session.copy(exercises = session.exercises + completedExercise)
        _uiState.update { it.copy(session = updatedSession) }
        viewModelScope.launch { workoutRepository.updateSession(updatedSession) }
    }

    private fun estimateCalories(seconds: Int): Int {
        // Aproximare: ~8-12 kcal/minut pentru antrenament cu greutăți
        return (seconds / 60.0 * 9).toInt()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        restTimerJob?.cancel()
    }
}
