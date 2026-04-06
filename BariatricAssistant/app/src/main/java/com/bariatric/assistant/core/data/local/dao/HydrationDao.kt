package com.bariatric.assistant.core.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bariatric.assistant.core.data.local.entity.HydrationEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HydrationDao {

    @Query("SELECT * FROM hydration_entries WHERE date = :date ORDER BY timestamp DESC")
    fun getEntriesByDate(date: String): Flow<List<HydrationEntryEntity>>

    @Query("SELECT COALESCE(SUM(amountMl), 0) FROM hydration_entries WHERE date = :date")
    fun getTotalForDate(date: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: HydrationEntryEntity): Long

    @Delete
    suspend fun deleteEntry(entry: HydrationEntryEntity)

    @Query("SELECT date, SUM(amountMl) as total FROM hydration_entries GROUP BY date ORDER BY date DESC LIMIT :days")
    fun getDailyTotals(days: Int = 7): Flow<List<DailyHydrationTotal>>
}

data class DailyHydrationTotal(
    val date: String,
    val total: Int
)
