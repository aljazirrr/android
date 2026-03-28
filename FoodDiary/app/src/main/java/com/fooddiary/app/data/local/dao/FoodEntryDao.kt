package com.fooddiary.app.data.local.dao

import androidx.room.*
import com.fooddiary.app.data.local.entity.FoodEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodEntryDao {
    @Query("SELECT * FROM food_entries WHERE dayDate = :date ORDER BY timeAdded ASC")
    fun getEntriesForDate(date: Long): Flow<List<FoodEntryEntity>>

    @Query("SELECT * FROM food_entries WHERE dayDate BETWEEN :start AND :end ORDER BY dayDate ASC, timeAdded ASC")
    fun getEntriesInRange(start: Long, end: Long): Flow<List<FoodEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: FoodEntryEntity)

    @Delete
    suspend fun deleteEntry(entry: FoodEntryEntity)

    @Query("DELETE FROM food_entries WHERE id = :entryId")
    suspend fun deleteEntryById(entryId: String)
}
