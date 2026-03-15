package com.fitlife.app.di

import android.content.Context
import androidx.room.Room
import com.fitlife.app.core.data.local.FitLifeDatabase
import com.fitlife.app.core.data.local.dao.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ─── Database ─────────────────────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FitLifeDatabase =
        Room.databaseBuilder(context, FitLifeDatabase::class.java, FitLifeDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideUserDao(db: FitLifeDatabase) = db.userDao()
    @Provides fun provideExerciseDao(db: FitLifeDatabase) = db.exerciseDao()
    @Provides fun provideWorkoutSessionDao(db: FitLifeDatabase) = db.workoutSessionDao()
    @Provides fun provideNutritionLogDao(db: FitLifeDatabase) = db.nutritionLogDao()
    @Provides fun provideFoodDao(db: FitLifeDatabase) = db.foodDao()
    @Provides fun provideBodyMeasurementDao(db: FitLifeDatabase) = db.bodyMeasurementDao()
    @Provides fun provideProgressPhotoDao(db: FitLifeDatabase) = db.progressPhotoDao()
    @Provides fun providePersonalRecordDao(db: FitLifeDatabase) = db.personalRecordDao()
    @Provides fun provideDailyStatsDao(db: FitLifeDatabase) = db.dailyStatsDao()

    // ─── Firebase ─────────────────────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()
}
