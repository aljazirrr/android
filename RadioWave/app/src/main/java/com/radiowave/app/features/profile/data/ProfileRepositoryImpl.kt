package com.radiowave.app.features.profile.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.radiowave.app.core.domain.model.UserProfile
import com.radiowave.app.features.profile.domain.ProfileRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ProfileRepository {

    override fun getUserProfile(): Flow<UserProfile?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser
            trySend(
                user?.let {
                    UserProfile(
                        uid = it.uid,
                        email = it.email ?: "",
                        displayName = it.displayName ?: "",
                        photoUrl = it.photoUrl?.toString()
                    )
                }
            )
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun updateProfile(displayName: String, photoUrl: String?): Result<Unit> =
        runCatching {
            val user = firebaseAuth.currentUser ?: throw Exception("Not signed in")
            val request = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            user.updateProfile(request).await()
        }

    override suspend fun updateFavoriteGenres(genres: List<String>) {
        val uid = firebaseAuth.currentUser?.uid ?: return
        firestore.collection("users").document(uid)
            .update("favoriteGenres", genres)
            .await()
    }

    override suspend fun deleteAccount(): Result<Unit> = runCatching {
        val user = firebaseAuth.currentUser ?: throw Exception("Not signed in")
        user.delete().await()
    }
}
