package com.fitlife.app.features.auth.data

import com.fitlife.app.core.data.local.dao.UserDao
import com.fitlife.app.core.data.local.entities.UserEntity
import com.fitlife.app.core.data.remote.firebase.FirestoreCollections
import com.fitlife.app.core.domain.model.User
import com.fitlife.app.core.utils.*
import com.fitlife.app.features.auth.domain.repository.AuthRepository
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao
) : AuthRepository {

    override val currentUser: FirebaseUser? get() = auth.currentUser
    override val isLoggedIn: Boolean get() = auth.currentUser != null

    override fun getCurrentUserFlow(): Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                firestore.collection(FirestoreCollections.USERS).document(user.uid)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Timber.e(error, "Error listening to user")
                            return@addSnapshotListener
                        }
                        val domainUser = snapshot?.toObject(User::class.java)
                        trySend(domainUser)
                    }
            } else {
                trySend(null)
            }
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signIn(email: String, password: String): Resource<User> = try {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val firebaseUser = result.user ?: return Resource.Error("Login eșuat")
        val user = fetchAndCacheUser(firebaseUser)
        Resource.Success(user)
    } catch (e: Exception) {
        Timber.e(e, "signIn error")
        Resource.Error(mapFirebaseError(e), e)
    }

    override suspend fun register(
        email: String, password: String, displayName: String
    ): Resource<User> = try {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = result.user ?: return Resource.Error("Înregistrare eșuată")

        val user = User(
            uid = firebaseUser.uid,
            email = email,
            displayName = displayName,
            createdAt = System.currentTimeMillis(),
            lastActiveAt = System.currentTimeMillis()
        )
        saveUserToFirestore(user)
        cacheUser(user)
        Resource.Success(user)
    } catch (e: Exception) {
        Timber.e(e, "register error")
        Resource.Error(mapFirebaseError(e), e)
    }

    override suspend fun signInWithGoogle(idToken: String): Resource<User> = try {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        val firebaseUser = result.user ?: return Resource.Error("Google login eșuat")

        val existingUser = getFirestoreUser(firebaseUser.uid)
        val user = existingUser ?: User(
            uid = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            displayName = firebaseUser.displayName ?: "",
            photoUrl = firebaseUser.photoUrl?.toString(),
            createdAt = System.currentTimeMillis(),
            lastActiveAt = System.currentTimeMillis()
        )

        if (existingUser == null) saveUserToFirestore(user)
        cacheUser(user)
        Resource.Success(user)
    } catch (e: Exception) {
        Timber.e(e, "signInWithGoogle error")
        Resource.Error(mapFirebaseError(e), e)
    }

    override suspend fun sendPasswordResetEmail(email: String): Resource<Unit> = try {
        auth.sendPasswordResetEmail(email).await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(mapFirebaseError(e), e)
    }

    override suspend fun updatePassword(oldPassword: String, newPassword: String): Resource<Unit> = try {
        val user = auth.currentUser ?: return Resource.Error("Nu ești autentificat")
        val credential = EmailAuthProvider.getCredential(user.email!!, oldPassword)
        user.reauthenticate(credential).await()
        user.updatePassword(newPassword).await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(mapFirebaseError(e), e)
    }

    override suspend fun deleteAccount(): Resource<Unit> = try {
        val user = auth.currentUser ?: return Resource.Error("Nu ești autentificat")
        firestore.collection(FirestoreCollections.USERS).document(user.uid).delete().await()
        userDao.deleteUser(user.uid)
        user.delete().await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(mapFirebaseError(e), e)
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun refreshUser(): Resource<User> = try {
        val firebaseUser = auth.currentUser ?: return Resource.Error("Nu ești autentificat")
        val user = fetchAndCacheUser(firebaseUser)
        Resource.Success(user)
    } catch (e: Exception) {
        Resource.Error(mapFirebaseError(e), e)
    }

    private suspend fun fetchAndCacheUser(firebaseUser: FirebaseUser): User {
        val user = getFirestoreUser(firebaseUser.uid) ?: User(
            uid = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            displayName = firebaseUser.displayName ?: "",
            photoUrl = firebaseUser.photoUrl?.toString()
        )
        cacheUser(user)
        return user
    }

    private suspend fun getFirestoreUser(uid: String): User? = try {
        val snapshot = firestore.collection(FirestoreCollections.USERS).document(uid).get().await()
        snapshot.toObject(User::class.java)
    } catch (e: Exception) {
        null
    }

    private suspend fun saveUserToFirestore(user: User) {
        firestore.collection(FirestoreCollections.USERS).document(user.uid).set(user).await()
    }

    private suspend fun cacheUser(user: User) {
        userDao.insertUser(
            UserEntity(
                uid = user.uid,
                email = user.email,
                displayName = user.displayName,
                photoUrl = user.photoUrl,
                age = user.age,
                gender = user.gender,
                heightCm = user.heightCm,
                weightKg = user.weightKg,
                fitnessLevel = user.fitnessLevel,
                weeklyWorkoutTarget = user.weeklyWorkoutTarget,
                dailyCalorieTarget = user.dailyCalorieTarget,
                dailyStepsTarget = user.dailyStepsTarget,
                dailyWaterTargetMl = user.dailyWaterTargetMl,
                createdAt = user.createdAt,
                lastActiveAt = user.lastActiveAt
            )
        )
    }

    private fun mapFirebaseError(e: Exception): String = when {
        e.message?.contains("EMAIL_EXISTS") == true ||
        e.message?.contains("email-already-in-use") == true -> "Emailul este deja folosit"
        e.message?.contains("INVALID_EMAIL") == true ||
        e.message?.contains("invalid-email") == true -> "Email invalid"
        e.message?.contains("WRONG_PASSWORD") == true ||
        e.message?.contains("wrong-password") == true -> "Parolă incorectă"
        e.message?.contains("USER_NOT_FOUND") == true ||
        e.message?.contains("user-not-found") == true -> "Contul nu există"
        e.message?.contains("WEAK_PASSWORD") == true ||
        e.message?.contains("weak-password") == true -> "Parola este prea slabă"
        e.message?.contains("NETWORK_ERROR") == true -> "Eroare de rețea"
        e.message?.contains("TOO_MANY_REQUESTS") == true -> "Prea multe încercări. Încearcă mai târziu"
        else -> e.message ?: "Eroare necunoscută"
    }
}
