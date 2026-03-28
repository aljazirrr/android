package com.fooddiary.app.data.local.dao

import androidx.room.*
import com.fooddiary.app.data.local.entity.DailyLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyLogDao {
    @Query("SELECT * FROM daily_logs WHERE date = :date LIMIT 1")
    fun getLogForDate(date: Long): Flow<DailyLogEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(log: DailyLogEntity)

    @Query("UPDATE daily_logs SET waterMl = waterMl + :amount WHERE date = :date")
    suspend fun addWater(date: Long, amount: Int)

    @Query("UPDATE daily_logs SET notes = :notes WHERE date = :date")
    suspend fun updateNotes(date: Long, notes: String)
}
