package com.radiowave.app.features.auth.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.radiowave.app.core.domain.model.UserProfile
import com.radiowave.app.features.auth.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override val currentUser: Flow<UserProfile?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.toUserProfile())
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<UserProfile> =
        runCatching {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            result.user?.toUserProfile() ?: throw Exception("Sign in failed")
        }

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String
    ): Result<UserProfile> = runCatching {
        val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user ?: throw Exception("Registration failed")
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .build()
        user.updateProfile(profileUpdates).await()
        user.reload().await()
        firebaseAuth.currentUser?.toUserProfile() ?: throw Exception("Profile update failed")
    }

    override suspend fun signInWithGoogle(idToken: String): Result<UserProfile> = runCatching {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = firebaseAuth.signInWithCredential(credential).await()
        result.user?.toUserProfile() ?: throw Exception("Google sign in failed")
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> = runCatching {
        firebaseAuth.sendPasswordResetEmail(email).await()
    }

    override fun isSignedIn(): Boolean = firebaseAuth.currentUser != null

    override fun getCurrentUserId(): String? = firebaseAuth.currentUser?.uid

    private fun com.google.firebase.auth.FirebaseUser.toUserProfile() = UserProfile(
        uid = uid,
        email = email ?: "",
        displayName = displayName ?: email?.substringBefore("@") ?: "User",
        photoUrl = photoUrl?.toString()
    )
}
