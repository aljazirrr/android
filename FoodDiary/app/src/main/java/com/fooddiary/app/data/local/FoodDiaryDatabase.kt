package com.fooddiary.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.fooddiary.app.data.local.dao.DailyLogDao
import com.fooddiary.app.data.local.dao.FoodDao
import com.fooddiary.app.data.local.dao.FoodEntryDao
import com.fooddiary.app.data.local.entity.DailyLogEntity
import com.fooddiary.app.data.local.entity.FoodEntity
import com.fooddiary.app.data.local.entity.FoodEntryEntity

@Database(
    entities = [FoodEntity::class, FoodEntryEntity::class, DailyLogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class FoodDiaryDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
    abstract fun foodEntryDao(): FoodEntryDao
    abstract fun dailyLogDao(): DailyLogDao
}
