package com.bariatric.assistant.features.hydration

import com.bariatric.assistant.core.data.local.dao.DailyHydrationTotal
import com.bariatric.assistant.core.data.local.dao.HydrationDao
import com.bariatric.assistant.core.data.local.entity.HydrationEntryEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HydrationRepository @Inject constructor(
    private val hydrationDao: HydrationDao
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun getTodayTotal(): Flow<Int> {
        return hydrationDao.getTotalForDate(today())
    }

    fun getTodayEntries(): Flow<List<HydrationEntryEntity>> {
        return hydrationDao.getEntriesByDate(today())
    }

    fun getWeeklyTotals(): Flow<List<DailyHydrationTotal>> {
        return hydrationDao.getDailyTotals(7)
    }

    suspend fun addWater(amountMl: Int) {
        hydrationDao.insertEntry(
            HydrationEntryEntity(
                amountMl = amountMl,
                date = today()
            )
        )
    }

    suspend fun removeEntry(entry: HydrationEntryEntity) {
        hydrationDao.deleteEntry(entry)
    }

    private fun today(): String = LocalDate.now().format(dateFormatter)
}
