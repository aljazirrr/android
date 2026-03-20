package com.radiowave.app.di

import com.radiowave.app.features.archive.data.ArchiveRepositoryImpl
import com.radiowave.app.features.archive.domain.ArchiveRepository
import com.radiowave.app.features.auth.data.AuthRepositoryImpl
import com.radiowave.app.features.auth.domain.repository.AuthRepository
import com.radiowave.app.features.favorites.data.FavoritesRepositoryImpl
import com.radiowave.app.features.favorites.domain.FavoritesRepository
import com.radiowave.app.features.home.data.HomeRepositoryImpl
import com.radiowave.app.features.home.domain.HomeRepository
import com.radiowave.app.features.player.data.PlayerRepositoryImpl
import com.radiowave.app.features.player.domain.PlayerRepository
import com.radiowave.app.features.profile.data.ProfileRepositoryImpl
import com.radiowave.app.features.profile.domain.ProfileRepository
import com.radiowave.app.features.recommendations.data.RecommendationsRepositoryImpl
import com.radiowave.app.features.recommendations.domain.RecommendationsRepository
import com.radiowave.app.features.recording.data.RecordingRepositoryImpl
import com.radiowave.app.features.recording.domain.RecordingRepository
import com.radiowave.app.features.schedule.data.ScheduleRepositoryImpl
import com.radiowave.app.features.schedule.domain.ScheduleRepository
import com.radiowave.app.features.search.data.SearchRepositoryImpl
import com.radiowave.app.features.search.domain.SearchRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds @Singleton
    abstract fun bindHomeRepository(impl: HomeRepositoryImpl): HomeRepository

    @Binds @Singleton
    abstract fun bindSearchRepository(impl: SearchRepositoryImpl): SearchRepository

    @Binds @Singleton
    abstract fun bindPlayerRepository(impl: PlayerRepositoryImpl): PlayerRepository

    @Binds @Singleton
    abstract fun bindFavoritesRepository(impl: FavoritesRepositoryImpl): FavoritesRepository

    @Binds @Singleton
    abstract fun bindRecordingRepository(impl: RecordingRepositoryImpl): RecordingRepository

    @Binds @Singleton
    abstract fun bindArchiveRepository(impl: ArchiveRepositoryImpl): ArchiveRepository

    @Binds @Singleton
    abstract fun bindScheduleRepository(impl: ScheduleRepositoryImpl): ScheduleRepository

    @Binds @Singleton
    abstract fun bindRecommendationsRepository(impl: RecommendationsRepositoryImpl): RecommendationsRepository

    @Binds @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository
}
