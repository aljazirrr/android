package com.fooddiary.app.domain.model

enum class MealType(val displayName: String) {
    BREAKFAST("Mic dejun"),
    LUNCH("Prânz"),
    DINNER("Cină"),
    SNACK("Gustare")
}

data class Food(
    val id: String,
    val name: String,
    val brand: String? = null,
    val caloriesPer100g: Int,
    val proteinPer100g: Float,
    val carbsPer100g: Float,
    val fatPer100g: Float,
    val fiberPer100g: Float,
    val sugarPer100g: Float,
    val sodiumMgPer100g: Float
)

data class FoodEntry(
    val id: String,
    val dayDate: Long,
    val mealType: MealType,
    val foodId: String,
    val foodName: String,
    val quantityGrams: Float,
    val calories: Int,
    val proteinG: Float,
    val carbsG: Float,
    val fatG: Float,
    val fiberG: Float,
    val sugarG: Float,
    val sodiumMg: Float,
    val timeAdded: Long
)

data class DailyLog(
    val date: Long,
    val waterMl: Int = 0,
    val notes: String = ""
)

data class DaySummary(
    val date: Long,
    val dayLabel: String,
    val entries: List<FoodEntry>,
    val totalCalories: Int,
    val totalProteinG: Float,
    val totalCarbsG: Float,
    val totalFatG: Float,
    val totalFiberG: Float,
    val totalSugarG: Float,
    val waterMl: Int,
    val mealBreakdown: Map<MealType, List<FoodEntry>>
)

data class WeeklySummary(
    val weekStart: Long,
    val weekEnd: Long,
    val days: List<DaySummary>,
    val daysLogged: Int,
    val avgCalories: Int,
    val avgProteinG: Float,
    val avgCarbsG: Float,
    val avgFatG: Float,
    val avgFiberG: Float,
    val totalWaterMl: Int,
    val avgWaterMl: Int,
    val bestDay: DaySummary?,
    val worstDay: DaySummary?,
    val tips: List<NutritionTip>
)

data class NutritionTip(
    val icon: String,
    val message: String,
    val severity: TipSeverity
)

enum class TipSeverity { WARNING, INFO, GOOD }
