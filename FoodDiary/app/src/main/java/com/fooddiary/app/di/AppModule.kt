package com.fooddiary.app.di

import android.content.Context
import androidx.room.Room
import com.fooddiary.app.data.local.FoodDiaryDatabase
import com.fooddiary.app.data.local.dao.DailyLogDao
import com.fooddiary.app.data.local.dao.FoodDao
import com.fooddiary.app.data.local.dao.FoodEntryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FoodDiaryDatabase =
        Room.databaseBuilder(context, FoodDiaryDatabase::class.java, "food_diary_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideFoodDao(db: FoodDiaryDatabase): FoodDao = db.foodDao()
    @Provides fun provideFoodEntryDao(db: FoodDiaryDatabase): FoodEntryDao = db.foodEntryDao()
    @Provides fun provideDailyLogDao(db: FoodDiaryDatabase): DailyLogDao = db.dailyLogDao()
}
