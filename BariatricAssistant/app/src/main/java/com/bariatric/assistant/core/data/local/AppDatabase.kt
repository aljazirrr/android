package com.bariatric.assistant.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bariatric.assistant.core.data.local.dao.ChatDao
import com.bariatric.assistant.core.data.local.dao.FoodJournalDao
import com.bariatric.assistant.core.data.local.dao.HydrationDao
import com.bariatric.assistant.core.data.local.entity.ChatMessageEntity
import com.bariatric.assistant.core.data.local.entity.FoodJournalEntryEntity
import com.bariatric.assistant.core.data.local.entity.HydrationEntryEntity

@Database(
    entities = [
        ChatMessageEntity::class,
        HydrationEntryEntity::class,
        FoodJournalEntryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun hydrationDao(): HydrationDao
    abstract fun foodJournalDao(): FoodJournalDao
}
