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

    override fun getUser(userId: String): Flow<User?> =
        userDao.getUserById(userId).map { entity ->
            entity?.toDomain()
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
