package com.fitlife.app.features.workout.data.local

import com.fitlife.app.core.data.local.dao.WorkoutSessionDao
import com.fitlife.app.core.data.local.entities.WorkoutSessionEntity
import com.fitlife.app.core.data.remote.firebase.FirestoreCollections
import com.fitlife.app.core.domain.model.*
import com.fitlife.app.core.utils.*
import com.fitlife.app.features.workout.domain.repository.WorkoutRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class WorkoutRepositoryImpl @Inject constructor(
    private val workoutSessionDao: WorkoutSessionDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : WorkoutRepository {

    private val gson = Gson()

    override fun getSessionsByUser(userId: String): Flow<List<WorkoutSession>> =
        workoutSessionDao.getSessionsByUser(userId).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getCompletedSessions(userId: String): Flow<List<WorkoutSession>> =
        workoutSessionDao.getCompletedSessions(userId).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getRecentSessions(userId: String, limit: Int): Flow<List<WorkoutSession>> =
        workoutSessionDao.getCompletedSessions(userId).map { entities ->
            entities.take(limit).map { it.toDomain() }
        }

    override fun getSessionsInRange(userId: String, start: Long, end: Long): Flow<List<WorkoutSession>> =
        workoutSessionDao.getSessionsInRange(userId, start, end).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getActiveSession(userId: String): Flow<WorkoutSession?> =
        workoutSessionDao.getActiveSession(userId).map { it?.toDomain() }

    override fun getWorkoutStreak(userId: String): Flow<WorkoutStreak> =
        workoutSessionDao.getCompletedSessions(userId).map { entities ->
            calculateStreak(entities.map { it.toDomain() }, userId)
        }

    override suspend fun createSession(session: WorkoutSession): Resource<WorkoutSession> = try {
        val entity = session.toEntity()
        workoutSessionDao.insertSession(entity)
        syncToFirestore(session)
        Resource.Success(session)
    } catch (e: Exception) {
        Timber.e(e, "createSession error")
        Resource.Error(e.message ?: "Eroare la salvare", e)
    }

    override suspend fun updateSession(session: WorkoutSession): Resource<Unit> = try {
        workoutSessionDao.updateSession(session.toEntity())
        syncToFirestore(session)
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Eroare la actualizare", e)
    }

    override suspend fun deleteSession(sessionId: String): Resource<Unit> = try {
        val entity = workoutSessionDao.getSessionById(sessionId) ?: return Resource.Error("Nu s-a găsit sesiunea")
        workoutSessionDao.deleteSession(entity)
        auth.currentUser?.uid?.let { uid ->
            firestore.collection(FirestoreCollections.USERS).document(uid)
                .collection(FirestoreCollections.WORKOUT_SESSIONS).document(sessionId).delete().await()
        }
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Eroare la ștergere", e)
    }

    override fun getWorkoutPlans(userId: String): Flow<List<WorkoutPlan>> = flow {
        try {
            val snapshot = firestore.collection(FirestoreCollections.USERS).document(userId)
                .collection(FirestoreCollections.WORKOUT_PLANS).get().await()
            val plans = snapshot.documents.mapNotNull { it.toObject(WorkoutPlan::class.java) }
            emit(plans)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun createPlan(plan: WorkoutPlan): Resource<WorkoutPlan> = try {
        auth.currentUser?.uid?.let { uid ->
            firestore.collection(FirestoreCollections.USERS).document(uid)
                .collection(FirestoreCollections.WORKOUT_PLANS).document(plan.id).set(plan).await()
        }
        Resource.Success(plan)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Eroare", e)
    }

    override suspend fun updatePlan(plan: WorkoutPlan): Resource<Unit> = try {
        auth.currentUser?.uid?.let { uid ->
            firestore.collection(FirestoreCollections.USERS).document(uid)
                .collection(FirestoreCollections.WORKOUT_PLANS).document(plan.id).set(plan).await()
        }
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Eroare", e)
    }

    override suspend fun deletePlan(planId: String): Resource<Unit> = try {
        auth.currentUser?.uid?.let { uid ->
            firestore.collection(FirestoreCollections.USERS).document(uid)
                .collection(FirestoreCollections.WORKOUT_PLANS).document(planId).delete().await()
        }
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Eroare", e)
    }

    private suspend fun syncToFirestore(session: WorkoutSession) {
        try {
            auth.currentUser?.uid?.let { uid ->
                firestore.collection(FirestoreCollections.USERS).document(uid)
                    .collection(FirestoreCollections.WORKOUT_SESSIONS).document(session.id).set(session).await()
            }
        } catch (e: Exception) {
            Timber.w(e, "Sync to Firestore failed, cached locally")
        }
    }

    private fun calculateStreak(sessions: List<WorkoutSession>, userId: String): WorkoutStreak {
        if (sessions.isEmpty()) return WorkoutStreak()

        val completedDates = sessions
            .filter { it.status == WorkoutStatus.COMPLETED }
            .mapNotNull { it.completedAt?.startOfDay() }
            .distinct()
            .sortedDescending()

        if (completedDates.isEmpty()) return WorkoutStreak(totalWorkouts = sessions.size)

        var currentStreak = 0
        var longestStreak = 0
        var tempStreak = 1
        val todayStart = System.currentTimeMillis().startOfDay()
        val yesterdayStart = todayStart - TimeUnit.DAYS.toMillis(1)

        // Check if streak is still alive
        if (completedDates.first() >= yesterdayStart) {
            currentStreak = 1
            for (i in 1 until completedDates.size) {
                val diff = completedDates[i - 1] - completedDates[i]
                if (diff <= TimeUnit.DAYS.toMillis(1) + 1000) {
                    currentStreak++
                } else break
            }
        }

        // Calculate longest streak
        for (i in 1 until completedDates.size) {
            val diff = completedDates[i - 1] - completedDates[i]
            if (diff <= TimeUnit.DAYS.toMillis(1) + 1000) {
                tempStreak++
                longestStreak = maxOf(longestStreak, tempStreak)
            } else {
                tempStreak = 1
            }
        }
        longestStreak = maxOf(longestStreak, currentStreak)

        val cal = Calendar.getInstance()
        val weekStart = cal.apply { set(Calendar.DAY_OF_WEEK, firstDayOfWeek) }.timeInMillis.startOfDay()
        val monthStart = cal.apply { set(Calendar.DAY_OF_MONTH, 1) }.timeInMillis.startOfDay()

        return WorkoutStreak(
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            lastWorkoutDate = completedDates.firstOrNull(),
            totalWorkouts = sessions.size,
            thisWeekWorkouts = completedDates.count { it >= weekStart },
            thisMonthWorkouts = completedDates.count { it >= monthStart }
        )
    }

    private fun WorkoutSessionEntity.toDomain(): WorkoutSession {
        val exercises: List<CompletedExercise> = try {
            gson.fromJson(exercisesJson, object : TypeToken<List<CompletedExercise>>() {}.type)
        } catch (e: Exception) { emptyList() }
        return WorkoutSession(
            id = id, userId = userId, planId = planId, workoutDayId = workoutDayId,
            name = name, status = status, startTime = startTime, endTime = endTime,
            durationSeconds = durationSeconds, exercises = exercises,
            totalVolume = totalVolume, caloriesBurned = caloriesBurned,
            notes = notes, rating = rating, scheduledDate = scheduledDate, completedAt = completedAt
        )
    }

    private fun WorkoutSession.toEntity() = WorkoutSessionEntity(
        id = id, userId = userId, planId = planId, workoutDayId = workoutDayId,
        name = name, status = status, startTime = startTime, endTime = endTime,
        durationSeconds = durationSeconds, exercisesJson = gson.toJson(exercises),
        totalVolume = totalVolume, caloriesBurned = caloriesBurned,
        notes = notes, rating = rating, scheduledDate = scheduledDate, completedAt = completedAt
    )
}
