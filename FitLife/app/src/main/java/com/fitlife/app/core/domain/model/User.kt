package com.fitlife.app.core.domain.model

import com.fitlife.app.core.utils.FitnessLevel
import com.fitlife.app.core.utils.Gender

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String? = null,
    val age: Int = 0,
    val gender: Gender = Gender.NOT_SPECIFIED,
    val heightCm: Float = 0f,
    val weightKg: Float = 0f,
    val fitnessLevel: FitnessLevel = FitnessLevel.BEGINNER,
    val goals: List<FitnessGoal> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val lastActiveAt: Long = System.currentTimeMillis(),
    val weeklyWorkoutTarget: Int = 3,
    val dailyCalorieTarget: Int = 2000,
    val dailyStepsTarget: Int = 10000,
    val dailyWaterTargetMl: Int = 2500
)

data class FitnessGoal(
    val id: String = "",
    val type: GoalType = GoalType.WEIGHT_LOSS,
    val targetValue: Float = 0f,
    val currentValue: Float = 0f,
    val unit: String = "",
    val deadline: Long? = null,
    val isCompleted: Boolean = false
)

enum class GoalType {
    WEIGHT_LOSS, MUSCLE_GAIN, ENDURANCE, STRENGTH, FLEXIBILITY, GENERAL_FITNESS
}
