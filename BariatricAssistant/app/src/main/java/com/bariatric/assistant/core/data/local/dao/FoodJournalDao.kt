package com.bariatric.assistant.core.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bariatric.assistant.core.data.local.entity.FoodJournalEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodJournalDao {

    @Query("SELECT * FROM food_journal_entries WHERE date = :date ORDER BY timestamp DESC")
    fun getEntriesByDate(date: String): Flow<List<FoodJournalEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: FoodJournalEntryEntity): Long

    @Delete
    suspend fun deleteEntry(entry: FoodJournalEntryEntity)

    @Query("SELECT COUNT(*) FROM food_journal_entries WHERE date = :date")
    fun getEntryCountForDate(date: String): Flow<Int>
}
