package com.fitlife.app.features.nutrition.domain

import com.fitlife.app.core.domain.model.*
import com.fitlife.app.core.utils.Resource
import kotlinx.coroutines.flow.Flow

interface NutritionRepository {
    fun getNutritionLogsByUser(userId: String): Flow<List<NutritionLog>>
    fun getNutritionLogForDate(userId: String, date: Long): Flow<NutritionLog?>
    fun getNutritionLogsInRange(userId: String, start: Long, end: Long): Flow<List<NutritionLog>>
    fun searchFoods(query: String): Flow<List<Food>>
    fun getAllFoods(): Flow<List<Food>>
    suspend fun getFoodByBarcode(barcode: String): Food?
    suspend fun addFoodEntry(userId: String, date: Long, mealType: com.fitlife.app.core.utils.MealType, entry: FoodEntry): Resource<Unit>
    suspend fun removeFoodEntry(userId: String, date: Long, mealId: String, entryId: String): Resource<Unit>
    suspend fun logWater(userId: String, amountMl: Int): Resource<Unit>
    suspend fun createFood(food: Food): Resource<Food>
    suspend fun seedDefaultFoods()
}
