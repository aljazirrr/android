package com.fitlife.app.core.domain.model

import com.fitlife.app.core.utils.MuscleGroup
import com.fitlife.app.core.utils.WorkoutStatus

data class WorkoutPlan(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val description: String = "",
    val durationWeeks: Int = 4,
    val sessionsPerWeek: Int = 3,
    val days: List<WorkoutDay> = emptyList(),
    val isCustom: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class WorkoutDay(
    val id: String = "",
    val name: String = "",
    val dayOfWeek: Int = 0,
    val exercises: List<WorkoutExercise> = emptyList(),
    val estimatedDurationMinutes: Int = 45
)

data class WorkoutExercise(
    val id: String = "",
    val exerciseId: String = "",
    val exerciseName: String = "",
    val sets: Int = 3,
    val reps: Int? = 10,
    val durationSeconds: Int? = null,
    val restSeconds: Int = 60,
    val weightKg: Float? = null,
    val notes: String = "",
    val order: Int = 0
)

data class WorkoutSession(
    val id: String = "",
    val userId: String = "",
    val planId: String? = null,
    val workoutDayId: String? = null,
    val name: String = "",
    val status: WorkoutStatus = WorkoutStatus.PLANNED,
    val startTime: Long? = null,
    val endTime: Long? = null,
    val durationSeconds: Int = 0,
    val exercises: List<CompletedExercise> = emptyList(),
    val totalVolume: Float = 0f,
    val caloriesBurned: Int = 0,
    val notes: String = "",
    val rating: Int = 0,
    val scheduledDate: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

data class CompletedExercise(
    val id: String = "",
    val exerciseId: String = "",
    val exerciseName: String = "",
    val sets: List<CompletedSet> = emptyList(),
    val order: Int = 0,
    val notes: String = ""
)

data class CompletedSet(
    val setNumber: Int = 1,
    val reps: Int? = null,
    val weightKg: Float? = null,
    val durationSeconds: Int? = null,
    val isCompleted: Boolean = false,
    val rpe: Int? = null
)

data class Exercise(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val instructions: List<String> = emptyList(),
    val muscleGroups: List<MuscleGroup> = emptyList(),
    val secondaryMuscles: List<MuscleGroup> = emptyList(),
    val equipment: com.fitlife.app.core.utils.Equipment = com.fitlife.app.core.utils.Equipment.NONE,
    val type: com.fitlife.app.core.utils.ExerciseType = com.fitlife.app.core.utils.ExerciseType.STRENGTH,
    val difficulty: Int = 1,
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    val isCustom: Boolean = false,
    val createdBy: String? = null
)
