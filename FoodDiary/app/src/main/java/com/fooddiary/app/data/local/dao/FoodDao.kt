package com.fooddiary.app.data.local.dao

import androidx.room.*
import com.fooddiary.app.data.local.entity.FoodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Query("SELECT * FROM foods ORDER BY name ASC")
    fun getAllFoods(): Flow<List<FoodEntity>>

    @Query("SELECT * FROM foods WHERE name LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%'")
    fun searchFoods(query: String): Flow<List<FoodEntity>>

    @Query("SELECT * FROM foods WHERE id = :id LIMIT 1")
    suspend fun getFoodById(id: String): FoodEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoods(foods: List<FoodEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: FoodEntity)

    @Query("SELECT COUNT(*) FROM foods")
    suspend fun getFoodCount(): Int
}
