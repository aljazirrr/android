package com.fitlife.app.core.domain.model

data class BodyMeasurement(
    val id: String = "",
    val userId: String = "",
    val date: Long = System.currentTimeMillis(),
    val weightKg: Float? = null,
    val bodyFatPercent: Float? = null,
    val muscleMassKg: Float? = null,
    val bmi: Float? = null,
    val chestCm: Float? = null,
    val waistCm: Float? = null,
    val hipsCm: Float? = null,
    val leftArmCm: Float? = null,
    val rightArmCm: Float? = null,
    val leftThighCm: Float? = null,
    val rightThighCm: Float? = null,
    val leftCalfCm: Float? = null,
    val rightCalfCm: Float? = null,
    val notes: String = ""
)

data class ProgressPhoto(
    val id: String = "",
    val userId: String = "",
    val date: Long = System.currentTimeMillis(),
    val photoUrl: String = "",
    val thumbnailUrl: String? = null,
    val angle: PhotoAngle = PhotoAngle.FRONT,
    val notes: String = ""
)

enum class PhotoAngle { FRONT, BACK, SIDE_LEFT, SIDE_RIGHT }

data class PersonalRecord(
    val id: String = "",
    val userId: String = "",
    val exerciseId: String = "",
    val exerciseName: String = "",
    val value: Float = 0f,
    val unit: String = "kg",
    val recordType: RecordType = RecordType.MAX_WEIGHT,
    val achievedAt: Long = System.currentTimeMillis()
)

enum class RecordType { MAX_WEIGHT, MAX_REPS, MAX_VOLUME, FASTEST_TIME, LONGEST_DISTANCE }

data class DailyStats(
    val date: Long = System.currentTimeMillis(),
    val userId: String = "",
    val steps: Int = 0,
    val caloriesBurned: Int = 0,
    val caloriesConsumed: Int = 0,
    val activeMinutes: Int = 0,
    val workoutCompleted: Boolean = false,
    val waterMl: Int = 0,
    val sleepHours: Float = 0f,
    val restingHeartRate: Int? = null
)

data class WorkoutStreak(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastWorkoutDate: Long? = null,
    val totalWorkouts: Int = 0,
    val thisWeekWorkouts: Int = 0,
    val thisMonthWorkouts: Int = 0
)
