package com.fitlife.app.core.domain.model

import com.fitlife.app.core.utils.MealType

data class NutritionLog(
    val id: String = "",
    val userId: String = "",
    val date: Long = System.currentTimeMillis(),
    val meals: List<Meal> = emptyList(),
    val waterMl: Int = 0,
    val totalCalories: Int = 0,
    val totalProteinG: Float = 0f,
    val totalCarbsG: Float = 0f,
    val totalFatG: Float = 0f,
    val totalFiberG: Float = 0f
)

data class Meal(
    val id: String = "",
    val type: MealType = MealType.BREAKFAST,
    val name: String = "",
    val time: Long = System.currentTimeMillis(),
    val foods: List<FoodEntry> = emptyList(),
    val totalCalories: Int = 0
)

data class FoodEntry(
    val id: String = "",
    val foodId: String = "",
    val foodName: String = "",
    val quantity: Float = 100f,
    val unit: com.fitlife.app.core.utils.NutritionUnit = com.fitlife.app.core.utils.NutritionUnit.GRAMS,
    val calories: Int = 0,
    val proteinG: Float = 0f,
    val carbsG: Float = 0f,
    val fatG: Float = 0f,
    val fiberG: Float = 0f
)

data class Food(
    val id: String = "",
    val name: String = "",
    val brand: String? = null,
    val barcode: String? = null,
    val servingSize: Float = 100f,
    val servingUnit: com.fitlife.app.core.utils.NutritionUnit = com.fitlife.app.core.utils.NutritionUnit.GRAMS,
    val caloriesPer100g: Int = 0,
    val proteinPer100g: Float = 0f,
    val carbsPer100g: Float = 0f,
    val fatPer100g: Float = 0f,
    val fiberPer100g: Float = 0f,
    val sugarPer100g: Float = 0f,
    val sodiumPer100g: Float = 0f,
    val isVerified: Boolean = false
)

data class WaterLog(
    val id: String = "",
    val userId: String = "",
    val date: Long = System.currentTimeMillis(),
    val amountMl: Int = 250,
    val time: Long = System.currentTimeMillis()
)
