package com.fitlife.app.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.fitlife.app.core.data.local.dao.*
import com.fitlife.app.core.data.local.entities.*

@Database(
    entities = [
        UserEntity::class,
        ExerciseEntity::class,
        WorkoutSessionEntity::class,
        NutritionLogEntity::class,
        FoodEntity::class,
        BodyMeasurementEntity::class,
        ProgressPhotoEntity::class,
        PersonalRecordEntity::class,
        DailyStatsEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class FitLifeDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutSessionDao(): WorkoutSessionDao
    abstract fun nutritionLogDao(): NutritionLogDao
    abstract fun foodDao(): FoodDao
    abstract fun bodyMeasurementDao(): BodyMeasurementDao
    abstract fun progressPhotoDao(): ProgressPhotoDao
    abstract fun personalRecordDao(): PersonalRecordDao
    abstract fun dailyStatsDao(): DailyStatsDao

    companion object {
        const val DATABASE_NAME = "fitlife_db"
    }
}
