package com.fitlife.app.core.data.local.dao

import androidx.room.*
import com.fitlife.app.core.data.local.entities.*
import kotlinx.coroutines.flow.Flow

// ─── User DAO ────────────────────────────────────────────────────────────────

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE uid = :uid")
    fun getUserById(uid: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    suspend fun getUserByIdOnce(uid: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users WHERE uid = :uid")
    suspend fun deleteUser(uid: String)
}

// ─── Exercise DAO ─────────────────────────────────────────────────────────────

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises ORDER BY name ASC")
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getExerciseById(id: String): ExerciseEntity?

    @Query("SELECT * FROM exercises WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchExercises(query: String): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE isCustom = 1 AND createdBy = :userId")
    fun getCustomExercises(userId: String): Flow<List<ExerciseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<ExerciseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: ExerciseEntity)

    @Delete
    suspend fun deleteExercise(exercise: ExerciseEntity)

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun getExerciseCount(): Int
}

// ─── Workout Session DAO ──────────────────────────────────────────────────────

@Dao
interface WorkoutSessionDao {
    @Query("SELECT * FROM workout_sessions WHERE userId = :userId ORDER BY scheduledDate DESC")
    fun getSessionsByUser(userId: String): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions WHERE userId = :userId AND status = 'COMPLETED' ORDER BY completedAt DESC")
    fun getCompletedSessions(userId: String): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions WHERE userId = :userId AND scheduledDate BETWEEN :start AND :end")
    fun getSessionsInRange(userId: String, start: Long, end: Long): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    suspend fun getSessionById(id: String): WorkoutSessionEntity?

    @Query("SELECT * FROM workout_sessions WHERE userId = :userId AND status = 'IN_PROGRESS' LIMIT 1")
    fun getActiveSession(userId: String): Flow<WorkoutSessionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSessionEntity)

    @Update
    suspend fun updateSession(session: WorkoutSessionEntity)

    @Delete
    suspend fun deleteSession(session: WorkoutSessionEntity)

    @Query("SELECT COUNT(*) FROM workout_sessions WHERE userId = :userId AND status = 'COMPLETED'")
    fun getTotalCompletedWorkouts(userId: String): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM workout_sessions
        WHERE userId = :userId AND status = 'COMPLETED'
        AND completedAt BETWEEN :weekStart AND :weekEnd
    """)
    fun getWorkoutsThisWeek(userId: String, weekStart: Long, weekEnd: Long): Flow<Int>
}

// ─── Nutrition DAO ────────────────────────────────────────────────────────────

@Dao
interface NutritionLogDao {
    @Query("SELECT * FROM nutrition_logs WHERE userId = :userId ORDER BY date DESC")
    fun getLogsByUser(userId: String): Flow<List<NutritionLogEntity>>

    @Query("SELECT * FROM nutrition_logs WHERE userId = :userId AND date BETWEEN :start AND :end")
    fun getLogsInRange(userId: String, start: Long, end: Long): Flow<List<NutritionLogEntity>>

    @Query("SELECT * FROM nutrition_logs WHERE userId = :userId AND date = :date LIMIT 1")
    fun getLogForDate(userId: String, date: Long): Flow<NutritionLogEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: NutritionLogEntity)

    @Update
    suspend fun updateLog(log: NutritionLogEntity)

    @Delete
    suspend fun deleteLog(log: NutritionLogEntity)
}

@Dao
interface FoodDao {
    @Query("SELECT * FROM foods ORDER BY name ASC")
    fun getAllFoods(): Flow<List<FoodEntity>>

    @Query("SELECT * FROM foods WHERE name LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%'")
    fun searchFoods(query: String): Flow<List<FoodEntity>>

    @Query("SELECT * FROM foods WHERE barcode = :barcode LIMIT 1")
    suspend fun getFoodByBarcode(barcode: String): FoodEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoods(foods: List<FoodEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: FoodEntity)
}

// ─── Progress DAO ─────────────────────────────────────────────────────────────

@Dao
interface BodyMeasurementDao {
    @Query("SELECT * FROM body_measurements WHERE userId = :userId ORDER BY date DESC")
    fun getMeasurementsByUser(userId: String): Flow<List<BodyMeasurementEntity>>

    @Query("SELECT * FROM body_measurements WHERE userId = :userId ORDER BY date DESC LIMIT 1")
    fun getLatestMeasurement(userId: String): Flow<BodyMeasurementEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurement(measurement: BodyMeasurementEntity)

    @Update
    suspend fun updateMeasurement(measurement: BodyMeasurementEntity)

    @Delete
    suspend fun deleteMeasurement(measurement: BodyMeasurementEntity)
}

@Dao
interface ProgressPhotoDao {
    @Query("SELECT * FROM progress_photos WHERE userId = :userId ORDER BY date DESC")
    fun getPhotosByUser(userId: String): Flow<List<ProgressPhotoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: ProgressPhotoEntity)

    @Delete
    suspend fun deletePhoto(photo: ProgressPhotoEntity)
}

@Dao
interface PersonalRecordDao {
    @Query("SELECT * FROM personal_records WHERE userId = :userId ORDER BY achievedAt DESC")
    fun getRecordsByUser(userId: String): Flow<List<PersonalRecordEntity>>

    @Query("SELECT * FROM personal_records WHERE userId = :userId AND exerciseId = :exerciseId")
    fun getRecordsForExercise(userId: String, exerciseId: String): Flow<List<PersonalRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: PersonalRecordEntity)

    @Delete
    suspend fun deleteRecord(record: PersonalRecordEntity)
}

@Dao
interface DailyStatsDao {
    @Query("SELECT * FROM daily_stats WHERE userId = :userId ORDER BY date DESC")
    fun getStatsByUser(userId: String): Flow<List<DailyStatsEntity>>

    @Query("SELECT * FROM daily_stats WHERE userId = :userId AND date = :date LIMIT 1")
    fun getStatsForDate(userId: String, date: Long): Flow<DailyStatsEntity?>

    @Query("SELECT * FROM daily_stats WHERE userId = :userId AND date BETWEEN :start AND :end ORDER BY date ASC")
    fun getStatsInRange(userId: String, start: Long, end: Long): Flow<List<DailyStatsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStats(stats: DailyStatsEntity)

    @Update
    suspend fun updateStats(stats: DailyStatsEntity)
}
