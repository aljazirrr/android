package com.fitlife.app.di

import com.fitlife.app.core.data.local.dao.*
import com.fitlife.app.features.auth.data.AuthRepositoryImpl
import com.fitlife.app.features.auth.domain.repository.AuthRepository
import com.fitlife.app.features.workout.data.local.WorkoutRepositoryImpl
import com.fitlife.app.features.workout.domain.repository.WorkoutRepository
import com.fitlife.app.features.exercises.data.ExerciseRepositoryImpl
import com.fitlife.app.features.exercises.domain.ExerciseRepository
import com.fitlife.app.features.nutrition.data.NutritionRepositoryImpl
import com.fitlife.app.features.nutrition.domain.NutritionRepository
import com.fitlife.app.features.progress.data.ProgressRepositoryImpl
import com.fitlife.app.features.progress.domain.ProgressRepository
import com.fitlife.app.features.profile.data.ProfileRepositoryImpl
import com.fitlife.app.features.profile.domain.ProfileRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore,
        userDao: UserDao
    ): AuthRepository = AuthRepositoryImpl(auth, firestore, userDao)

    @Provides
    @Singleton
    fun provideWorkoutRepository(
        workoutSessionDao: WorkoutSessionDao,
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): WorkoutRepository = WorkoutRepositoryImpl(workoutSessionDao, firestore, auth)

    @Provides
    @Singleton
    fun provideExerciseRepository(
        exerciseDao: ExerciseDao,
        firestore: FirebaseFirestore
    ): ExerciseRepository = ExerciseRepositoryImpl(exerciseDao, firestore)

    @Provides
    @Singleton
    fun provideNutritionRepository(
        nutritionLogDao: NutritionLogDao,
        foodDao: FoodDao,
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): NutritionRepository = NutritionRepositoryImpl(nutritionLogDao, foodDao, firestore, auth)

    @Provides
    @Singleton
    fun provideProgressRepository(
        bodyMeasurementDao: BodyMeasurementDao,
        progressPhotoDao: ProgressPhotoDao,
        personalRecordDao: PersonalRecordDao,
        dailyStatsDao: DailyStatsDao,
        firestore: FirebaseFirestore,
        storage: FirebaseStorage,
        auth: FirebaseAuth
    ): ProgressRepository = ProgressRepositoryImpl(
        bodyMeasurementDao, progressPhotoDao, personalRecordDao,
        dailyStatsDao, firestore, storage, auth
    )

    @Provides
    @Singleton
    fun provideProfileRepository(
        userDao: UserDao,
        firestore: FirebaseFirestore,
        storage: FirebaseStorage,
        auth: FirebaseAuth
    ): ProfileRepository = ProfileRepositoryImpl(userDao, firestore, storage, auth)
}
