package com.fitlife.app.features.exercises.domain

import com.fitlife.app.core.domain.model.Exercise
import com.fitlife.app.core.utils.Equipment
import com.fitlife.app.core.utils.ExerciseType
import com.fitlife.app.core.utils.MuscleGroup
import com.fitlife.app.core.utils.Resource
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    fun getAllExercises(): Flow<List<Exercise>>
    fun searchExercises(query: String): Flow<List<Exercise>>
    fun getCustomExercises(userId: String): Flow<List<Exercise>>
    fun filterExercises(
        muscleGroup: MuscleGroup? = null,
        equipment: Equipment? = null,
        type: ExerciseType? = null
    ): Flow<List<Exercise>>
    suspend fun getExerciseById(id: String): Exercise?
    suspend fun createCustomExercise(exercise: Exercise): Resource<Exercise>
    suspend fun deleteCustomExercise(exerciseId: String): Resource<Unit>
    suspend fun seedDefaultExercises()
}
