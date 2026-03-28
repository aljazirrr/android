package com.fooddiary.app.data.repository

import com.fooddiary.app.data.local.dao.DailyLogDao
import com.fooddiary.app.data.local.dao.FoodDao
import com.fooddiary.app.data.local.dao.FoodEntryDao
import com.fooddiary.app.data.local.entity.DailyLogEntity
import com.fooddiary.app.data.local.entity.FoodEntity
import com.fooddiary.app.data.local.entity.FoodEntryEntity
import com.fooddiary.app.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodDiaryRepository @Inject constructor(
    private val foodDao: FoodDao,
    private val foodEntryDao: FoodEntryDao,
    private val dailyLogDao: DailyLogDao
) {
    // ─── Foods ───────────────────────────────────────────────────────────────

    fun getAllFoods(): Flow<List<Food>> =
        foodDao.getAllFoods().map { list -> list.map { it.toDomain() } }

    fun searchFoods(query: String): Flow<List<Food>> =
        foodDao.searchFoods(query).map { list -> list.map { it.toDomain() } }

    // ─── Food Entries ────────────────────────────────────────────────────────

    fun getEntriesForDate(date: Long): Flow<List<FoodEntry>> =
        foodEntryDao.getEntriesForDate(date).map { list -> list.map { it.toDomain() } }

    fun getEntriesInRange(start: Long, end: Long): Flow<List<FoodEntry>> =
        foodEntryDao.getEntriesInRange(start, end).map { list -> list.map { it.toDomain() } }

    suspend fun addFoodEntry(
        date: Long,
        mealType: MealType,
        food: Food,
        quantityGrams: Float
    ) {
        val factor = quantityGrams / 100f
        val entry = FoodEntryEntity(
            id = UUID.randomUUID().toString(),
            dayDate = date,
            mealType = mealType.name,
            foodId = food.id,
            foodName = food.name,
            quantityGrams = quantityGrams,
            calories = (food.caloriesPer100g * factor).toInt(),
            proteinG = food.proteinPer100g * factor,
            carbsG = food.carbsPer100g * factor,
            fatG = food.fatPer100g * factor,
            fiberG = food.fiberPer100g * factor,
            sugarG = food.sugarPer100g * factor,
            sodiumMg = food.sodiumMgPer100g * factor,
            timeAdded = System.currentTimeMillis()
        )
        foodEntryDao.insertEntry(entry)

        // Ensure daily log exists
        dailyLogDao.insertOrUpdate(DailyLogEntity(date = date))
    }

    suspend fun removeFoodEntry(entryId: String) {
        foodEntryDao.deleteEntryById(entryId)
    }

    // ─── Daily Log ───────────────────────────────────────────────────────────

    fun getDailyLog(date: Long): Flow<DailyLog?> =
        dailyLogDao.getLogForDate(date).map { it?.toDomain() }

    suspend fun addWater(date: Long, amountMl: Int) {
        dailyLogDao.insertOrUpdate(DailyLogEntity(date = date))
        dailyLogDao.addWater(date, amountMl)
    }

    suspend fun updateNotes(date: Long, notes: String) {
        dailyLogDao.insertOrUpdate(DailyLogEntity(date = date))
        dailyLogDao.updateNotes(date, notes)
    }

    // ─── Seed ────────────────────────────────────────────────────────────────

    suspend fun seedDefaultFoods() {
        if (foodDao.getFoodCount() > 0) return
        foodDao.insertFoods(defaultFoods)
    }
}

// ─── Mappers ─────────────────────────────────────────────────────────────────

private fun FoodEntity.toDomain() = Food(
    id = id, name = name, brand = brand,
    caloriesPer100g = caloriesPer100g,
    proteinPer100g = proteinPer100g, carbsPer100g = carbsPer100g,
    fatPer100g = fatPer100g, fiberPer100g = fiberPer100g,
    sugarPer100g = sugarPer100g, sodiumMgPer100g = sodiumMgPer100g
)

private fun FoodEntryEntity.toDomain() = FoodEntry(
    id = id, dayDate = dayDate, mealType = MealType.valueOf(mealType),
    foodId = foodId, foodName = foodName, quantityGrams = quantityGrams,
    calories = calories, proteinG = proteinG, carbsG = carbsG,
    fatG = fatG, fiberG = fiberG, sugarG = sugarG,
    sodiumMg = sodiumMg, timeAdded = timeAdded
)

private fun DailyLogEntity.toDomain() = DailyLog(
    date = date, waterMl = waterMl, notes = notes
)

// ─── Default Foods ───────────────────────────────────────────────────────────

private val defaultFoods = listOf(
    FoodEntity("1", "Piept de pui", null, 165, 31f, 0f, 3.6f, 0f, 0f, 74f),
    FoodEntity("2", "Orez alb fiert", null, 130, 2.7f, 28f, 0.3f, 0.4f, 0f, 1f),
    FoodEntity("3", "Ou fiert", null, 155, 13f, 1.1f, 11f, 0f, 1.1f, 124f),
    FoodEntity("4", "Banană", null, 89, 1.1f, 23f, 0.3f, 2.6f, 12f, 1f),
    FoodEntity("5", "Pâine integrală", null, 247, 13f, 41f, 3.4f, 7f, 6f, 400f),
    FoodEntity("6", "Iaurt grecesc", "Olympus", 97, 9f, 3.6f, 5f, 0f, 3.6f, 36f),
    FoodEntity("7", "Broccoli", null, 34, 2.8f, 7f, 0.4f, 2.6f, 1.7f, 33f),
    FoodEntity("8", "Somon", null, 208, 20f, 0f, 13f, 0f, 0f, 59f),
    FoodEntity("9", "Cartofi fierți", null, 87, 1.9f, 20f, 0.1f, 1.8f, 0.8f, 6f),
    FoodEntity("10", "Măr", null, 52, 0.3f, 14f, 0.2f, 2.4f, 10f, 1f),
    FoodEntity("11", "Ton conservă", "Calvo", 116, 26f, 0f, 1f, 0f, 0f, 300f),
    FoodEntity("12", "Lapte 1.5%", "Zuzu", 46, 3.4f, 5f, 1.5f, 0f, 5f, 44f),
    FoodEntity("13", "Brânză de vaci", null, 98, 11f, 3.4f, 4.3f, 0f, 2.7f, 364f),
    FoodEntity("14", "Fulgi de ovăz", null, 389, 17f, 66f, 7f, 11f, 0f, 2f),
    FoodEntity("15", "Migdale", null, 579, 21f, 22f, 50f, 12f, 4.4f, 1f),
    FoodEntity("16", "Paste integrale", null, 124, 5f, 25f, 0.5f, 3.2f, 0.6f, 1f),
    FoodEntity("17", "Roșii", null, 18, 0.9f, 3.9f, 0.2f, 1.2f, 2.6f, 5f),
    FoodEntity("18", "Castraveți", null, 15, 0.7f, 3.6f, 0.1f, 0.5f, 1.7f, 2f),
    FoodEntity("19", "Morcovi", null, 41, 0.9f, 10f, 0.2f, 2.8f, 4.7f, 69f),
    FoodEntity("20", "Avocado", null, 160, 2f, 9f, 15f, 7f, 0.7f, 7f),
    FoodEntity("21", "Miere", null, 304, 0.3f, 82f, 0f, 0.2f, 82f, 4f),
    FoodEntity("22", "Unt", null, 717, 0.9f, 0.1f, 81f, 0f, 0.1f, 11f),
    FoodEntity("23", "Ulei de măsline", null, 884, 0f, 0f, 100f, 0f, 0f, 2f),
    FoodEntity("24", "Ciocolată neagră 70%", null, 598, 8f, 46f, 43f, 11f, 24f, 20f),
    FoodEntity("25", "Fasole conservă", null, 94, 6.5f, 16f, 0.4f, 5.5f, 1.4f, 300f)
)
