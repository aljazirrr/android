package com.fitlife.app.core.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.fitlife.app.core.utils.*

// ─── Type Converters ────────────────────────────────────────────────────────

class Converters {
    private val gson = Gson()

    @TypeConverter fun fromStringList(value: String): List<String> =
        gson.fromJson(value, object : TypeToken<List<String>>() {}.type) ?: emptyList()

    @TypeConverter fun toStringList(list: List<String>): String = gson.toJson(list)

    @TypeConverter fun fromMuscleList(value: String): List<MuscleGroup> =
        gson.fromJson(value, object : TypeToken<List<MuscleGroup>>() {}.type) ?: emptyList()

    @TypeConverter fun toMuscleList(list: List<MuscleGroup>): String = gson.toJson(list)

    @TypeConverter fun fromGender(value: String): Gender = Gender.valueOf(value)
    @TypeConverter fun toGender(gender: Gender): String = gender.name

    @TypeConverter fun fromFitnessLevel(value: String): FitnessLevel = FitnessLevel.valueOf(value)
    @TypeConverter fun toFitnessLevel(level: FitnessLevel): String = level.name

    @TypeConverter fun fromEquipment(value: String): Equipment = Equipment.valueOf(value)
    @TypeConverter fun toEquipment(eq: Equipment): String = eq.name

    @TypeConverter fun fromExerciseType(value: String): ExerciseType = ExerciseType.valueOf(value)
    @TypeConverter fun toExerciseType(type: ExerciseType): String = type.name

    @TypeConverter fun fromWorkoutStatus(value: String): WorkoutStatus = WorkoutStatus.valueOf(value)
    @TypeConverter fun toWorkoutStatus(status: WorkoutStatus): String = status.name

    @TypeConverter fun fromMealType(value: String): MealType = MealType.valueOf(value)
    @TypeConverter fun toMealType(type: MealType): String = type.name

    @TypeConverter fun fromNutritionUnit(value: String): NutritionUnit = NutritionUnit.valueOf(value)
    @TypeConverter fun toNutritionUnit(unit: NutritionUnit): String = unit.name

    @TypeConverter fun fromPhotoAngle(value: String): com.fitlife.app.core.domain.model.PhotoAngle =
        com.fitlife.app.core.domain.model.PhotoAngle.valueOf(value)
    @TypeConverter fun toPhotoAngle(angle: com.fitlife.app.core.domain.model.PhotoAngle): String = angle.name

    @TypeConverter fun fromRecordType(value: String): com.fitlife.app.core.domain.model.RecordType =
        com.fitlife.app.core.domain.model.RecordType.valueOf(value)
    @TypeConverter fun toRecordType(type: com.fitlife.app.core.domain.model.RecordType): String = type.name
}

// ─── User ────────────────────────────────────────────────────────────────────

@Entity(tableName = "users")
@TypeConverters(Converters::class)
data class UserEntity(
    @PrimaryKey val uid: String,
    val email: String,
    val displayName: String,
    val photoUrl: String?,
    val age: Int,
    val gender: Gender,
    val heightCm: Float,
    val weightKg: Float,
    val fitnessLevel: FitnessLevel,
    val weeklyWorkoutTarget: Int,
    val dailyCalorieTarget: Int,
    val dailyStepsTarget: Int,
    val dailyWaterTargetMl: Int,
    val createdAt: Long,
    val lastActiveAt: Long
)

// ─── Exercise ─────────────────────────────────────────────────────────────────

@Entity(tableName = "exercises")
@TypeConverters(Converters::class)
data class ExerciseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val instructions: List<String>,
    val muscleGroups: List<MuscleGroup>,
    val secondaryMuscles: List<MuscleGroup>,
    val equipment: Equipment,
    val type: ExerciseType,
    val difficulty: Int,
    val imageUrl: String?,
    val videoUrl: String?,
    val isCustom: Boolean,
    val createdBy: String?
)

// ─── Workout Session ─────────────────────────────────────────────────────────

@Entity(tableName = "workout_sessions")
@TypeConverters(Converters::class)
data class WorkoutSessionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val planId: String?,
    val workoutDayId: String?,
    val name: String,
    val status: WorkoutStatus,
    val startTime: Long?,
    val endTime: Long?,
    val durationSeconds: Int,
    val exercisesJson: String,
    val totalVolume: Float,
    val caloriesBurned: Int,
    val notes: String,
    val rating: Int,
    val scheduledDate: Long,
    val completedAt: Long?
)

// ─── Nutrition Log ────────────────────────────────────────────────────────────

@Entity(tableName = "nutrition_logs")
@TypeConverters(Converters::class)
data class NutritionLogEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val date: Long,
    val mealsJson: String,
    val waterMl: Int,
    val totalCalories: Int,
    val totalProteinG: Float,
    val totalCarbsG: Float,
    val totalFatG: Float,
    val totalFiberG: Float
)

// ─── Food ─────────────────────────────────────────────────────────────────────

@Entity(tableName = "foods")
@TypeConverters(Converters::class)
data class FoodEntity(
    @PrimaryKey val id: String,
    val name: String,
    val brand: String?,
    val barcode: String?,
    val servingSize: Float,
    val servingUnit: NutritionUnit,
    val caloriesPer100g: Int,
    val proteinPer100g: Float,
    val carbsPer100g: Float,
    val fatPer100g: Float,
    val fiberPer100g: Float,
    val sugarPer100g: Float,
    val sodiumPer100g: Float,
    val isVerified: Boolean
)

// ─── Body Measurement ─────────────────────────────────────────────────────────

@Entity(tableName = "body_measurements")
data class BodyMeasurementEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val date: Long,
    val weightKg: Float?,
    val bodyFatPercent: Float?,
    val muscleMassKg: Float?,
    val bmi: Float?,
    val chestCm: Float?,
    val waistCm: Float?,
    val hipsCm: Float?,
    val leftArmCm: Float?,
    val rightArmCm: Float?,
    val leftThighCm: Float?,
    val rightThighCm: Float?,
    val leftCalfCm: Float?,
    val rightCalfCm: Float?,
    val notes: String
)

// ─── Progress Photo ───────────────────────────────────────────────────────────

@Entity(tableName = "progress_photos")
@TypeConverters(Converters::class)
data class ProgressPhotoEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val date: Long,
    val photoUrl: String,
    val thumbnailUrl: String?,
    val angle: com.fitlife.app.core.domain.model.PhotoAngle,
    val notes: String
)

// ─── Personal Record ──────────────────────────────────────────────────────────

@Entity(tableName = "personal_records")
@TypeConverters(Converters::class)
data class PersonalRecordEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val exerciseId: String,
    val exerciseName: String,
    val value: Float,
    val unit: String,
    val recordType: com.fitlife.app.core.domain.model.RecordType,
    val achievedAt: Long
)

// ─── Daily Stats ──────────────────────────────────────────────────────────────

@Entity(tableName = "daily_stats", primaryKeys = ["userId", "date"])
data class DailyStatsEntity(
    val userId: String,
    val date: Long,
    val steps: Int,
    val caloriesBurned: Int,
    val caloriesConsumed: Int,
    val activeMinutes: Int,
    val workoutCompleted: Boolean,
    val waterMl: Int,
    val sleepHours: Float,
    val restingHeartRate: Int?
)
