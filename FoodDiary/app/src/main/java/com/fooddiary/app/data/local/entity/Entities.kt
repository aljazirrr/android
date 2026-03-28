package com.fooddiary.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "foods")
data class FoodEntity(
    @PrimaryKey val id: String,
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

@Entity(tableName = "food_entries")
data class FoodEntryEntity(
    @PrimaryKey val id: String,
    val dayDate: Long,
    val mealType: String,
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

@Entity(tableName = "daily_logs", primaryKeys = ["date"])
data class DailyLogEntity(
    val date: Long,
    val waterMl: Int = 0,
    val notes: String = ""
)
