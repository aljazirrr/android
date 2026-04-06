package com.bariatric.assistant.di

import android.content.Context
import androidx.room.Room
import com.bariatric.assistant.BuildConfig
import com.bariatric.assistant.core.data.local.AppDatabase
import com.bariatric.assistant.core.data.local.dao.ChatDao
import com.bariatric.assistant.core.data.local.dao.FoodJournalDao
import com.bariatric.assistant.core.data.local.dao.HydrationDao
import com.bariatric.assistant.core.data.preferences.UserPreferences
import com.google.ai.client.generativeai.GenerativeModel
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "bariatric_assistant_db"
        ).build()
    }

    @Provides
    fun provideChatDao(db: AppDatabase): ChatDao = db.chatDao()

    @Provides
    fun provideHydrationDao(db: AppDatabase): HydrationDao = db.hydrationDao()

    @Provides
    fun provideFoodJournalDao(db: AppDatabase): FoodJournalDao = db.foodJournalDao()

    @Provides
    @Singleton
    fun provideUserPreferences(@ApplicationContext context: Context): UserPreferences {
        return UserPreferences(context)
    }

    @Provides
    @Singleton
    fun provideGenerativeModel(): GenerativeModel {
        return GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = BuildConfig.GEMINI_API_KEY
        )
    }
}
