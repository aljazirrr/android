package com.fitlife.app.features.nutrition.data

import com.fitlife.app.core.data.local.dao.FoodDao
import com.fitlife.app.core.data.local.dao.NutritionLogDao
import com.fitlife.app.core.data.local.entities.FoodEntity
import com.fitlife.app.core.data.local.entities.NutritionLogEntity
import com.fitlife.app.core.data.remote.firebase.FirestoreCollections
import com.fitlife.app.core.domain.model.*
import com.fitlife.app.core.utils.*
import com.fitlife.app.features.nutrition.domain.NutritionRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class NutritionRepositoryImpl @Inject constructor(
    private val nutritionLogDao: NutritionLogDao,
    private val foodDao: FoodDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : NutritionRepository {

    private val gson = Gson()

    override fun getNutritionLogsByUser(userId: String): Flow<List<NutritionLog>> =
        nutritionLogDao.getLogsByUser(userId).map { it.map { e -> e.toDomain() } }

    override fun getNutritionLogForDate(userId: String, date: Long): Flow<NutritionLog?> =
        nutritionLogDao.getLogForDate(userId, date.startOfDay()).map { it?.toDomain() }

    override fun getNutritionLogsInRange(userId: String, start: Long, end: Long): Flow<List<NutritionLog>> =
        nutritionLogDao.getLogsInRange(userId, start, end).map { it.map { e -> e.toDomain() } }

    override fun searchFoods(query: String): Flow<List<Food>> =
        foodDao.searchFoods(query).map { it.map { e -> e.toDomain() } }

    override fun getAllFoods(): Flow<List<Food>> =
        foodDao.getAllFoods().map { it.map { e -> e.toDomain() } }

    override suspend fun getFoodByBarcode(barcode: String): Food? =
        foodDao.getFoodByBarcode(barcode)?.toDomain()

    override suspend fun addFoodEntry(
        userId: String, date: Long, mealType: MealType, entry: FoodEntry
    ): Resource<Unit> = try {
        val dateKey = date.startOfDay()
        val existingLog = nutritionLogDao.getLogForDate(userId, dateKey).first()

        val log = existingLog?.toDomain() ?: NutritionLog(
            id = generateUid(), userId = userId, date = dateKey
        )

        val meals = log.meals.toMutableList()
        val mealIndex = meals.indexOfFirst { it.type == mealType }

        if (mealIndex >= 0) {
            val meal = meals[mealIndex]
            meals[mealIndex] = meal.copy(
                foods = meal.foods + entry,
                totalCalories = meal.foods.sumOf { it.calories } + entry.calories
            )
        } else {
            meals.add(Meal(
                id = generateUid(),
                type = mealType,
                name = mealType.displayName(),
                foods = listOf(entry),
                totalCalories = entry.calories
            ))
        }

        val updatedLog = log.copy(
            meals = meals,
            totalCalories = meals.sumOf { it.totalCalories },
            totalProteinG = meals.flatMap { it.foods }.sumOf { it.proteinG.toDouble() }.toFloat(),
            totalCarbsG = meals.flatMap { it.foods }.sumOf { it.carbsG.toDouble() }.toFloat(),
            totalFatG = meals.flatMap { it.foods }.sumOf { it.fatG.toDouble() }.toFloat(),
            totalFiberG = meals.flatMap { it.foods }.sumOf { it.fiberG.toDouble() }.toFloat()
        )

        nutritionLogDao.insertLog(updatedLog.toEntity())
        syncNutritionToFirestore(updatedLog)
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Eroare", e)
    }

    override suspend fun removeFoodEntry(
        userId: String, date: Long, mealId: String, entryId: String
    ): Resource<Unit> = try {
        val dateKey = date.startOfDay()
        val existingLog = nutritionLogDao.getLogForDate(userId, dateKey).first()?.toDomain()
            ?: return Resource.Error("Log negăsit")

        val updatedMeals = existingLog.meals.map { meal ->
            if (meal.id == mealId) {
                val updatedFoods = meal.foods.filter { it.id != entryId }
                meal.copy(foods = updatedFoods, totalCalories = updatedFoods.sumOf { it.calories })
            } else meal
        }

        val updatedLog = existingLog.copy(
            meals = updatedMeals,
            totalCalories = updatedMeals.sumOf { it.totalCalories }
        )
        nutritionLogDao.updateLog(updatedLog.toEntity())
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Eroare", e)
    }

    override suspend fun logWater(userId: String, amountMl: Int): Resource<Unit> = try {
        val dateKey = System.currentTimeMillis().startOfDay()
        val existingLog = nutritionLogDao.getLogForDate(userId, dateKey).first()

        if (existingLog != null) {
            val updated = existingLog.copy(waterMl = existingLog.waterMl + amountMl)
            nutritionLogDao.updateLog(updated)
        } else {
            nutritionLogDao.insertLog(
                NutritionLogEntity(
                    id = generateUid(), userId = userId, date = dateKey,
                    mealsJson = "[]", waterMl = amountMl,
                    totalCalories = 0, totalProteinG = 0f,
                    totalCarbsG = 0f, totalFatG = 0f, totalFiberG = 0f
                )
            )
        }
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Eroare", e)
    }

    override suspend fun createFood(food: Food): Resource<Food> = try {
        foodDao.insertFood(food.toEntity())
        Resource.Success(food)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Eroare", e)
    }

    override suspend fun seedDefaultFoods() {
        val defaultFoods = listOf(
            Food(id = "chicken_breast", name = "Piept de pui (copt)", caloriesPer100g = 165, proteinPer100g = 31f, carbsPer100g = 0f, fatPer100g = 3.6f),
            Food(id = "oatmeal", name = "Fulgi de ovăz", caloriesPer100g = 389, proteinPer100g = 17f, carbsPer100g = 66f, fatPer100g = 7f, fiberPer100g = 10f),
            Food(id = "egg", name = "Ou (întreg)", caloriesPer100g = 155, proteinPer100g = 13f, carbsPer100g = 1f, fatPer100g = 11f),
            Food(id = "banana", name = "Banană", caloriesPer100g = 89, proteinPer100g = 1.1f, carbsPer100g = 23f, fatPer100g = 0.3f, fiberPer100g = 2.6f),
            Food(id = "rice", name = "Orez (gătit)", caloriesPer100g = 130, proteinPer100g = 2.7f, carbsPer100g = 28f, fatPer100g = 0.3f),
            Food(id = "broccoli", name = "Broccoli", caloriesPer100g = 34, proteinPer100g = 2.8f, carbsPer100g = 7f, fatPer100g = 0.4f, fiberPer100g = 2.6f),
            Food(id = "salmon", name = "Somon (gătit)", caloriesPer100g = 208, proteinPer100g = 20f, carbsPer100g = 0f, fatPer100g = 13f),
            Food(id = "greek_yogurt", name = "Iaurt grecesc (0%)", caloriesPer100g = 59, proteinPer100g = 10f, carbsPer100g = 3.6f, fatPer100g = 0.4f),
            Food(id = "almonds", name = "Migdale", caloriesPer100g = 579, proteinPer100g = 21f, carbsPer100g = 22f, fatPer100g = 50f, fiberPer100g = 12f),
            Food(id = "sweet_potato", name = "Cartof dulce", caloriesPer100g = 86, proteinPer100g = 1.6f, carbsPer100g = 20f, fatPer100g = 0.1f, fiberPer100g = 3f),
            Food(id = "tuna", name = "Ton (conservă)", caloriesPer100g = 132, proteinPer100g = 28f, carbsPer100g = 0f, fatPer100g = 1f),
            Food(id = "milk", name = "Lapte (1.5%)", caloriesPer100g = 46, proteinPer100g = 3.4f, carbsPer100g = 5f, fatPer100g = 1.5f),
            Food(id = "whole_wheat_bread", name = "Pâine integrală", caloriesPer100g = 247, proteinPer100g = 13f, carbsPer100g = 41f, fatPer100g = 4f, fiberPer100g = 7f),
            Food(id = "cottage_cheese", name = "Brânză cottage", caloriesPer100g = 98, proteinPer100g = 11f, carbsPer100g = 3.4f, fatPer100g = 4.3f),
            Food(id = "apple", name = "Măr", caloriesPer100g = 52, proteinPer100g = 0.3f, carbsPer100g = 14f, fatPer100g = 0.2f, fiberPer100g = 2.4f)
        )
        foodDao.insertFoods(defaultFoods.map { it.toEntity() })
    }

    private suspend fun syncNutritionToFirestore(log: NutritionLog) {
        try {
            auth.currentUser?.uid?.let { uid ->
                firestore.collection(FirestoreCollections.USERS).document(uid)
                    .collection(FirestoreCollections.NUTRITION_LOGS).document(log.id).set(log).await()
            }
        } catch (e: Exception) {
            Timber.w(e, "Sync nutrition failed")
        }
    }

    private fun MealType.displayName() = when (this) {
        MealType.BREAKFAST -> "Mic dejun"
        MealType.LUNCH -> "Prânz"
        MealType.DINNER -> "Cină"
        MealType.SNACK -> "Gustare"
        MealType.PRE_WORKOUT -> "Pre-antrenament"
        MealType.POST_WORKOUT -> "Post-antrenament"
    }

    private fun NutritionLogEntity.toDomain(): NutritionLog {
        val meals: List<Meal> = try {
            gson.fromJson(mealsJson, object : TypeToken<List<Meal>>() {}.type)
        } catch (e: Exception) { emptyList() }
        return NutritionLog(id = id, userId = userId, date = date, meals = meals,
            waterMl = waterMl, totalCalories = totalCalories, totalProteinG = totalProteinG,
            totalCarbsG = totalCarbsG, totalFatG = totalFatG, totalFiberG = totalFiberG)
    }

    private fun NutritionLog.toEntity() = NutritionLogEntity(
        id = id, userId = userId, date = date, mealsJson = gson.toJson(meals),
        waterMl = waterMl, totalCalories = totalCalories, totalProteinG = totalProteinG,
        totalCarbsG = totalCarbsG, totalFatG = totalFatG, totalFiberG = totalFiberG
    )

    private fun FoodEntity.toDomain() = Food(id = id, name = name, brand = brand, barcode = barcode,
        servingSize = servingSize, servingUnit = servingUnit, caloriesPer100g = caloriesPer100g,
        proteinPer100g = proteinPer100g, carbsPer100g = carbsPer100g, fatPer100g = fatPer100g,
        fiberPer100g = fiberPer100g, sugarPer100g = sugarPer100g, sodiumPer100g = sodiumPer100g,
        isVerified = isVerified)

    private fun Food.toEntity() = FoodEntity(id = id.ifEmpty { generateUid() }, name = name,
        brand = brand, barcode = barcode, servingSize = servingSize, servingUnit = servingUnit,
        caloriesPer100g = caloriesPer100g, proteinPer100g = proteinPer100g,
        carbsPer100g = carbsPer100g, fatPer100g = fatPer100g, fiberPer100g = fiberPer100g,
        sugarPer100g = sugarPer100g, sodiumPer100g = sodiumPer100g, isVerified = isVerified)
}
