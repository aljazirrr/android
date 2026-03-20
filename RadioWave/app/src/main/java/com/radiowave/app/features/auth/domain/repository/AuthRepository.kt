package com.radiowave.app.features.auth.domain.repository

import com.radiowave.app.core.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<UserProfile?>
    suspend fun signInWithEmail(email: String, password: String): Result<UserProfile>
    suspend fun signUpWithEmail(email: String, password: String, displayName: String): Result<UserProfile>
    suspend fun signInWithGoogle(idToken: String): Result<UserProfile>
    suspend fun signOut()
    suspend fun sendPasswordReset(email: String): Result<Unit>
    fun isSignedIn(): Boolean
    fun getCurrentUserId(): String?
}
