package com.fitlife.app.features.nutrition.domain

import com.fitlife.app.core.utils.MealType

data class WeeklySummaryData(
    val weekStartDate: Long,
    val weekEndDate: Long,
    val dailyLogs: List<DaySummary>,
    val averages: MacroAverages,
    val targets: MacroTargets,
    val bestDay: DaySummary?,
    val worstDay: DaySummary?,
    val totalWaterMl: Int,
    val avgWaterMl: Int,
    val daysLogged: Int,
    val tips: List<NutritionTip>
)

data class DaySummary(
    val date: Long,
    val dayOfWeekLabel: String,
    val calories: Int,
    val proteinG: Float,
    val carbsG: Float,
    val fatG: Float,
    val fiberG: Float,
    val waterMl: Int,
    val mealCount: Int,
    val hasMealType: Map<MealType, Boolean>
)

data class MacroAverages(
    val calories: Int,
    val proteinG: Float,
    val carbsG: Float,
    val fatG: Float,
    val fiberG: Float
)

data class MacroTargets(
    val calories: Int,
    val proteinG: Int,
    val carbsG: Int,
    val fatG: Int,
    val waterMl: Int
)

data class NutritionTip(
    val icon: String,
    val message: String,
    val severity: TipSeverity
)

enum class TipSeverity { INFO, WARNING, GOOD }
