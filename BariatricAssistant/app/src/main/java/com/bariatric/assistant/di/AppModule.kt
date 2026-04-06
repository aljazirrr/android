package com.bariatric.assistant.di

import android.content.Context
import androidx.room.Room
import com.bariatric.assistant.core.data.local.AppDatabase
import com.bariatric.assistant.core.data.local.dao.ChatDao
import com.bariatric.assistant.core.data.local.dao.FoodJournalDao
import com.bariatric.assistant.core.data.local.dao.HydrationDao
import com.bariatric.assistant.core.data.preferences.UserPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
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
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
}
