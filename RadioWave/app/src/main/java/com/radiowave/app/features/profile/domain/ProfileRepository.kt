package com.radiowave.app.features.profile.domain

import com.radiowave.app.core.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getUserProfile(): Flow<UserProfile?>
    suspend fun updateProfile(displayName: String, photoUrl: String?): Result<Unit>
    suspend fun updateFavoriteGenres(genres: List<String>)
    suspend fun deleteAccount(): Result<Unit>
}
