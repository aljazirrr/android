package com.fitlife.app.features.workout.domain.repository

import com.fitlife.app.core.domain.model.*
import com.fitlife.app.core.utils.Resource
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    fun getSessionsByUser(userId: String): Flow<List<WorkoutSession>>
    fun getCompletedSessions(userId: String): Flow<List<WorkoutSession>>
    fun getRecentSessions(userId: String, limit: Int): Flow<List<WorkoutSession>>
    fun getSessionsInRange(userId: String, start: Long, end: Long): Flow<List<WorkoutSession>>
    fun getActiveSession(userId: String): Flow<WorkoutSession?>
    fun getWorkoutStreak(userId: String): Flow<WorkoutStreak>
    suspend fun createSession(session: WorkoutSession): Resource<WorkoutSession>
    suspend fun updateSession(session: WorkoutSession): Resource<Unit>
    suspend fun deleteSession(sessionId: String): Resource<Unit>
    fun getWorkoutPlans(userId: String): Flow<List<WorkoutPlan>>
    suspend fun createPlan(plan: WorkoutPlan): Resource<WorkoutPlan>
    suspend fun updatePlan(plan: WorkoutPlan): Resource<Unit>
    suspend fun deletePlan(planId: String): Resource<Unit>
}
