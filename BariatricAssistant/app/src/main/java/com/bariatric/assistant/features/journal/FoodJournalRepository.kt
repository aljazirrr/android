package com.bariatric.assistant.features.journal

import com.bariatric.assistant.core.data.local.dao.FoodJournalDao
import com.bariatric.assistant.core.data.local.entity.FoodJournalEntryEntity
import com.bariatric.assistant.core.data.local.entity.MealType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodJournalRepository @Inject constructor(
    private val foodJournalDao: FoodJournalDao
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun getTodayEntries(): Flow<List<FoodJournalEntryEntity>> {
        return foodJournalDao.getEntriesByDate(today())
    }

    fun getTodayEntryCount(): Flow<Int> {
        return foodJournalDao.getEntryCountForDate(today())
    }

    suspend fun addEntry(
        foodName: String,
        mealType: MealType,
        portionDescription: String,
        notes: String?
    ) {
        foodJournalDao.insertEntry(
            FoodJournalEntryEntity(
                foodName = foodName,
                mealType = mealType,
                portionDescription = portionDescription,
                notes = notes,
                date = today()
            )
        )
    }

    suspend fun removeEntry(entry: FoodJournalEntryEntity) {
        foodJournalDao.deleteEntry(entry)
    }

    private fun today(): String = LocalDate.now().format(dateFormatter)
}
