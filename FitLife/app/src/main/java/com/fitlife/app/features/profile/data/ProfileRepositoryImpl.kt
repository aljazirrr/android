package com.fitlife.app.features.profile.data

import android.net.Uri
import com.fitlife.app.core.data.local.dao.UserDao
import com.fitlife.app.core.data.local.entities.UserEntity
import com.fitlife.app.core.data.remote.firebase.FirestoreCollections
import com.fitlife.app.core.domain.model.User
import com.fitlife.app.core.utils.Resource
import com.fitlife.app.features.profile.domain.ProfileRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import javax.inject.Inject



class ProfileRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
) : ProfileRepository {

    override fun getUser(userId: String): Flow<User?> = flow {
        // Emit from Room first
        val cached = userDao.getUserByIdOnce(userId)
        if (cached != null) {
            emit(cached.toDomain())
        } else {
            // Fetch from Firestore
            val user = try {
                val snapshot = firestore.collection(FirestoreCollections.USERS)
                    .document(userId).get().await()
                snapshot.toObject(User::class.java)
            } catch (e: Exception) {
                null
            }
            if (user != null) {
                userDao.insertUser(user.toEntity())
                emit(user)
            } else {
                // Final fallback: Firebase Auth data
                val firebaseUser = auth.currentUser
                val fallback = if (firebaseUser != null) {
                    User(
                        uid = firebaseUser.uid,
                        email = firebaseUser.email ?: "",
                        displayName = firebaseUser.displayName
                            ?: firebaseUser.email?.substringBefore("@") ?: "User"
                    )
                } else null
                if (fallback != null) userDao.insertUser(fallback.toEntity())
                emit(fallback)
            }
        }
        // Continue observing Room for updates
        emitAll(userDao.getUserById(userId).map { it?.toDomain() })
    }

    override suspend fun updateProfile(user: User): Resource<Unit> = try {
        userDao.updateUser(user.toEntity())
        firestore.collection(FirestoreCollections.USERS).document(user.uid).set(user).await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Eroare", e)
    }

    override suspend fun updateProfilePhoto(userId: String, photoUri: String): Resource<String> = try {
        val ref = storage.reference.child("profile_photos/$userId.jpg")
        ref.putFile(Uri.parse(photoUri)).await()
        val url = ref.downloadUrl.await().toString()
        firestore.collection(FirestoreCollections.USERS).document(userId)
            .update("photoUrl", url).await()
        Resource.Success(url)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Eroare upload", e)
    }

    override suspend fun deleteAccount(): Resource<Unit> = try {
        val userId = auth.currentUser?.uid ?: return Resource.Error("Nu ești autentificat")
        firestore.collection(FirestoreCollections.USERS).document(userId).delete().await()
        userDao.deleteUser(userId)
        auth.currentUser?.delete()?.await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Eroare", e)
    }

    private fun UserEntity.toDomain() = User(
        uid = uid, email = email, displayName = displayName, photoUrl = photoUrl,
        age = age, gender = gender, heightCm = heightCm, weightKg = weightKg,
        fitnessLevel = fitnessLevel, weeklyWorkoutTarget = weeklyWorkoutTarget,
        dailyCalorieTarget = dailyCalorieTarget, dailyStepsTarget = dailyStepsTarget,
        dailyWaterTargetMl = dailyWaterTargetMl, createdAt = createdAt, lastActiveAt = lastActiveAt
    )

    private fun User.toEntity() = UserEntity(
        uid = uid, email = email, displayName = displayName, photoUrl = photoUrl,
        age = age, gender = gender, heightCm = heightCm, weightKg = weightKg,
        fitnessLevel = fitnessLevel, weeklyWorkoutTarget = weeklyWorkoutTarget,
        dailyCalorieTarget = dailyCalorieTarget, dailyStepsTarget = dailyStepsTarget,
        dailyWaterTargetMl = dailyWaterTargetMl, createdAt = createdAt, lastActiveAt = lastActiveAt
    )
}
