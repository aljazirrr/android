package com.fitlife.app.features.profile.domain

import com.fitlife.app.core.domain.model.User
import com.fitlife.app.core.utils.Resource
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getUser(userId: String): Flow<User?>
    suspend fun updateProfile(user: User): Resource<Unit>
    suspend fun updateProfilePhoto(userId: String, photoUri: String): Resource<String>
    suspend fun deleteAccount(): Resource<Unit>
}
