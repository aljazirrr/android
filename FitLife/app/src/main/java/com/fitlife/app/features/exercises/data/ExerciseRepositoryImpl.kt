package com.fitlife.app.features.exercises.data

import com.fitlife.app.core.data.local.dao.ExerciseDao
import com.fitlife.app.core.data.local.entities.ExerciseEntity
import com.fitlife.app.core.data.remote.firebase.FirestoreCollections
import com.fitlife.app.core.domain.model.Exercise
import com.fitlife.app.core.utils.*
import com.fitlife.app.features.exercises.domain.ExerciseRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class ExerciseRepositoryImpl @Inject constructor(
    private val exerciseDao: ExerciseDao,
    private val firestore: FirebaseFirestore
) : ExerciseRepository {

    override fun getAllExercises(): Flow<List<Exercise>> =
        exerciseDao.getAllExercises().map { entities -> entities.map { it.toDomain() } }

    override fun searchExercises(query: String): Flow<List<Exercise>> =
        exerciseDao.searchExercises(query).map { entities -> entities.map { it.toDomain() } }

    override fun getCustomExercises(userId: String): Flow<List<Exercise>> =
        exerciseDao.getCustomExercises(userId).map { entities -> entities.map { it.toDomain() } }

    override fun filterExercises(
        muscleGroup: MuscleGroup?,
        equipment: Equipment?,
        type: ExerciseType?
    ): Flow<List<Exercise>> = getAllExercises().map { exercises ->
        exercises.filter { exercise ->
            (muscleGroup == null || exercise.muscleGroups.contains(muscleGroup)) &&
            (equipment == null || exercise.equipment == equipment) &&
            (type == null || exercise.type == type)
        }
    }

    override suspend fun getExerciseById(id: String): Exercise? =
        exerciseDao.getExerciseById(id)?.toDomain()

    override suspend fun createCustomExercise(exercise: Exercise): Resource<Exercise> = try {
        exerciseDao.insertExercise(exercise.toEntity())
        Resource.Success(exercise)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Eroare", e)
    }

    override suspend fun deleteCustomExercise(exerciseId: String): Resource<Unit> = try {
        val entity = exerciseDao.getExerciseById(exerciseId) ?: return Resource.Error("Nu s-a găsit")
        exerciseDao.deleteExercise(entity)
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Eroare", e)
    }

    override suspend fun seedDefaultExercises() {
        if (exerciseDao.getExerciseCount() > 0) return
        val exercises = getDefaultExercises()
        exerciseDao.insertExercises(exercises.map { it.toEntity() })
        Timber.d("Seeded ${exercises.size} default exercises")
    }

    private fun ExerciseEntity.toDomain() = Exercise(
        id = id, name = name, description = description,
        instructions = instructions, muscleGroups = muscleGroups,
        secondaryMuscles = secondaryMuscles, equipment = equipment,
        type = type, difficulty = difficulty, imageUrl = imageUrl,
        videoUrl = videoUrl, isCustom = isCustom, createdBy = createdBy
    )

    private fun Exercise.toEntity() = ExerciseEntity(
        id = id.ifEmpty { generateUid() }, name = name, description = description,
        instructions = instructions, muscleGroups = muscleGroups,
        secondaryMuscles = secondaryMuscles, equipment = equipment,
        type = type, difficulty = difficulty, imageUrl = imageUrl,
        videoUrl = videoUrl, isCustom = isCustom, createdBy = createdBy
    )

    private fun getDefaultExercises(): List<Exercise> = listOf(
        // ─── CHEST ─────────────────────────────────────────────────────────────
        Exercise(id = "bench_press", name = "Bench Press", description = "Exercițiu fundamental pentru piept",
            instructions = listOf("Întinde-te pe bancă", "Apucă bara la lățimea umerilor", "Coboară bara la piept", "Împinge bara sus"),
            muscleGroups = listOf(MuscleGroup.CHEST), secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS),
            equipment = Equipment.BARBELL, type = ExerciseType.STRENGTH, difficulty = 2),
        Exercise(id = "incline_press", name = "Incline Dumbbell Press", description = "Press pe bancă înclinată",
            instructions = listOf("Setează banca la 30-45 grade", "Tine gantere în mâini", "Coboară până la piept", "Împinge sus"),
            muscleGroups = listOf(MuscleGroup.CHEST), secondaryMuscles = listOf(MuscleGroup.TRICEPS),
            equipment = Equipment.DUMBBELL, type = ExerciseType.STRENGTH, difficulty = 2),
        Exercise(id = "push_up", name = "Push-Up", description = "Flotări clasice - nicio echipament necesară",
            instructions = listOf("Poziție plancă", "Coboară corpul", "Împinge sus"),
            muscleGroups = listOf(MuscleGroup.CHEST), secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.CORE),
            equipment = Equipment.BODYWEIGHT, type = ExerciseType.STRENGTH, difficulty = 1),
        // ─── BACK ──────────────────────────────────────────────────────────────
        Exercise(id = "pull_up", name = "Pull-Up", description = "Tracțiuni - exercițiu complet pentru spate",
            instructions = listOf("Apucă bara", "Trage-te sus până bărbia depășește bara", "Coboară controlat"),
            muscleGroups = listOf(MuscleGroup.BACK), secondaryMuscles = listOf(MuscleGroup.BICEPS),
            equipment = Equipment.PULL_UP_BAR, type = ExerciseType.STRENGTH, difficulty = 3),
        Exercise(id = "deadlift", name = "Deadlift", description = "Cel mai complet exercițiu de forță",
            instructions = listOf("Picioarele la lățimea umerilor", "Apucă bara", "Ridică cu spatele drept", "Coboară controlat"),
            muscleGroups = listOf(MuscleGroup.BACK), secondaryMuscles = listOf(MuscleGroup.HAMSTRINGS, MuscleGroup.GLUTES),
            equipment = Equipment.BARBELL, type = ExerciseType.STRENGTH, difficulty = 3),
        Exercise(id = "lat_pulldown", name = "Lat Pulldown", description = "Tracțiuni la cablu pentru spate larg",
            instructions = listOf("Stai pe scaun", "Apucă bara larg", "Trage bara la piept", "Ridică controlat"),
            muscleGroups = listOf(MuscleGroup.BACK), secondaryMuscles = listOf(MuscleGroup.BICEPS),
            equipment = Equipment.CABLE, type = ExerciseType.STRENGTH, difficulty = 2),
        Exercise(id = "bent_over_row", name = "Bent Over Row", description = "Ramat aplecat pentru densitate spate",
            instructions = listOf("Aplecă-te la 45 grade", "Apucă bara", "Trage la abdomen", "Coboară controlat"),
            muscleGroups = listOf(MuscleGroup.BACK), secondaryMuscles = listOf(MuscleGroup.BICEPS),
            equipment = Equipment.BARBELL, type = ExerciseType.STRENGTH, difficulty = 2),
        // ─── SHOULDERS ─────────────────────────────────────────────────────────
        Exercise(id = "ohp", name = "Overhead Press", description = "Presă deasupra capului pentru umeri",
            instructions = listOf("Bara la nivelul umerilor", "Împinge bara sus", "Coboară la bărbie"),
            muscleGroups = listOf(MuscleGroup.SHOULDERS), secondaryMuscles = listOf(MuscleGroup.TRICEPS),
            equipment = Equipment.BARBELL, type = ExerciseType.STRENGTH, difficulty = 2),
        Exercise(id = "lateral_raise", name = "Lateral Raise", description = "Ridicări laterale pentru capul mijlociu al deltoidului",
            instructions = listOf("Stai drept", "Ridică gantere lateral până la nivelul umerilor", "Coboară controlat"),
            muscleGroups = listOf(MuscleGroup.SHOULDERS),
            equipment = Equipment.DUMBBELL, type = ExerciseType.STRENGTH, difficulty = 1),
        // ─── LEGS ──────────────────────────────────────────────────────────────
        Exercise(id = "squat", name = "Squat", description = "Rege exercițiilor - squat cu bara",
            instructions = listOf("Bara pe umeri", "Picioarele la lățimea umerilor", "Coboară până coapsele sunt paralele", "Ridică-te"),
            muscleGroups = listOf(MuscleGroup.QUADRICEPS), secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS),
            equipment = Equipment.BARBELL, type = ExerciseType.STRENGTH, difficulty = 3),
        Exercise(id = "romanian_dl", name = "Romanian Deadlift", description = "Deadlift românesc pentru ischiogambieri",
            instructions = listOf("Stai cu picioarele la lățimea umerilor", "Coboară bara pe lângă picioare", "Simte stretch-ul în ischiogambieri"),
            muscleGroups = listOf(MuscleGroup.HAMSTRINGS), secondaryMuscles = listOf(MuscleGroup.GLUTES),
            equipment = Equipment.BARBELL, type = ExerciseType.STRENGTH, difficulty = 2),
        Exercise(id = "leg_press", name = "Leg Press", description = "Presă pentru picioare la aparat",
            instructions = listOf("Stai pe scaun", "Pune picioarele pe platformă", "Împinge platforma", "Coboară controlat"),
            muscleGroups = listOf(MuscleGroup.QUADRICEPS), secondaryMuscles = listOf(MuscleGroup.GLUTES),
            equipment = Equipment.MACHINE, type = ExerciseType.STRENGTH, difficulty = 1),
        Exercise(id = "lunge", name = "Lunge", description = "Atacuri pentru picioare",
            instructions = listOf("Stai drept", "Fă un pas mare înainte", "Coboară genunchiul din spate spre sol", "Ridică-te"),
            muscleGroups = listOf(MuscleGroup.QUADRICEPS), secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS),
            equipment = Equipment.BODYWEIGHT, type = ExerciseType.STRENGTH, difficulty = 1),
        // ─── ARMS ──────────────────────────────────────────────────────────────
        Exercise(id = "bicep_curl", name = "Bicep Curl", description = "Flexii pentru biceps",
            instructions = listOf("Stai drept", "Ridică gantere cu coatele fixe", "Coboară controlat"),
            muscleGroups = listOf(MuscleGroup.BICEPS),
            equipment = Equipment.DUMBBELL, type = ExerciseType.STRENGTH, difficulty = 1),
        Exercise(id = "tricep_dip", name = "Tricep Dip", description = "Dips pentru triceps",
            instructions = listOf("Apucă bara paralelă", "Coboară corpul", "Împinge sus"),
            muscleGroups = listOf(MuscleGroup.TRICEPS), secondaryMuscles = listOf(MuscleGroup.CHEST),
            equipment = Equipment.BODYWEIGHT, type = ExerciseType.STRENGTH, difficulty = 2),
        // ─── CORE ──────────────────────────────────────────────────────────────
        Exercise(id = "plank", name = "Plank", description = "Plancă pentru core",
            instructions = listOf("Poziție plancă pe coate", "Menține spatele drept", "Ține poziția"),
            muscleGroups = listOf(MuscleGroup.CORE),
            equipment = Equipment.BODYWEIGHT, type = ExerciseType.STRENGTH, difficulty = 1),
        Exercise(id = "crunch", name = "Crunch", description = "Abdomene clasice",
            instructions = listOf("Întinde-te pe spate", "Mâinile la cap", "Ridică umerii de la sol", "Coboară controlat"),
            muscleGroups = listOf(MuscleGroup.CORE),
            equipment = Equipment.BODYWEIGHT, type = ExerciseType.STRENGTH, difficulty = 1),
        // ─── CARDIO ────────────────────────────────────────────────────────────
        Exercise(id = "running", name = "Alergare", description = "Cardio fundamental",
            instructions = listOf("Menține ritm constant", "Respiră regulat"),
            muscleGroups = listOf(MuscleGroup.CARDIO),
            equipment = Equipment.NONE, type = ExerciseType.CARDIO, difficulty = 1),
        Exercise(id = "cycling", name = "Ciclism", description = "Cardio pe bicicletă",
            instructions = listOf("Ajustează șaua", "Pedalează la ritm constant"),
            muscleGroups = listOf(MuscleGroup.CARDIO),
            equipment = Equipment.BICYCLE, type = ExerciseType.CARDIO, difficulty = 1),
        Exercise(id = "jump_rope", name = "Coarda", description = "Cardio intens cu coarda",
            instructions = listOf("Sari corda în ritm constant"),
            muscleGroups = listOf(MuscleGroup.CARDIO), secondaryMuscles = listOf(MuscleGroup.CALVES),
            equipment = Equipment.NONE, type = ExerciseType.CARDIO, difficulty = 2)
    )
}
