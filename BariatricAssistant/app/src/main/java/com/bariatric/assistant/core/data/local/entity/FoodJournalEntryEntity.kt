package com.bariatric.assistant.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MealType {
    BREAKFAST, LUNCH, DINNER, SNACK
}

@Entity(tableName = "food_journal_entries")
data class FoodJournalEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val foodName: String,
    val mealType: MealType,
    val portionDescription: String,
    val notes: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val date: String // "yyyy-MM-dd"
)
