package com.fitlife.app.features.auth.domain.repository

import com.fitlife.app.core.domain.model.User
import com.fitlife.app.core.utils.Resource
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: FirebaseUser?
    val isLoggedIn: Boolean
    fun getCurrentUserFlow(): Flow<User?>
    suspend fun signIn(email: String, password: String): Resource<User>
    suspend fun register(email: String, password: String, displayName: String): Resource<User>
    suspend fun signInWithGoogle(idToken: String): Resource<User>
    suspend fun sendPasswordResetEmail(email: String): Resource<Unit>
    suspend fun updatePassword(oldPassword: String, newPassword: String): Resource<Unit>
    suspend fun deleteAccount(): Resource<Unit>
    suspend fun signOut()
    suspend fun refreshUser(): Resource<User>
}
