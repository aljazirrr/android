package com.fitlife.app.features.progress.data

import android.net.Uri
import com.fitlife.app.core.data.local.dao.*
import com.fitlife.app.core.data.local.entities.*
import com.fitlife.app.core.data.remote.firebase.FirestoreCollections
import com.fitlife.app.core.domain.model.*
import com.fitlife.app.core.utils.*
import com.fitlife.app.features.progress.domain.ProgressRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class ProgressRepositoryImpl @Inject constructor(
    private val bodyMeasurementDao: BodyMeasurementDao,
    private val progressPhotoDao: ProgressPhotoDao,
    private val personalRecordDao: PersonalRecordDao,
    private val dailyStatsDao: DailyStatsDao,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
) : ProgressRepository {

    override fun getMeasurementsByUser(userId: String): Flow<List<BodyMeasurement>> =
        bodyMeasurementDao.getMeasurementsByUser(userId).map { it.map { e -> e.toDomain() } }

    override fun getLatestMeasurement(userId: String): Flow<BodyMeasurement?> =
        bodyMeasurementDao.getLatestMeasurement(userId).map { it?.toDomain() }

    override fun getPhotosByUser(userId: String): Flow<List<ProgressPhoto>> =
        progressPhotoDao.getPhotosByUser(userId).map { it.map { e -> e.toDomain() } }

    override fun getPersonalRecords(userId: String): Flow<List<PersonalRecord>> =
        personalRecordDao.getRecordsByUser(userId).map { it.map { e -> e.toDomain() } }

    override fun getStatsForDate(userId: String, date: Long): Flow<DailyStats?> =
        dailyStatsDao.getStatsForDate(userId, date.startOfDay()).map { it?.toDomain() }

    override fun getStatsInRange(userId: String, start: Long, end: Long): Flow<List<DailyStats>> =
        dailyStatsDao.getStatsInRange(userId, start, end).map { it.map { e -> e.toDomain() } }

    override suspend fun addMeasurement(measurement: BodyMeasurement): Resource<Unit> = try {
        bodyMeasurementDao.insertMeasurement(measurement.toEntity())
        syncMeasurementToFirestore(measurement)
        Resource.Success(Unit)
    } catch (e: Exception) { Resource.Error(e.message ?: "Eroare", e) }

    override suspend fun deleteMeasurement(measurementId: String): Resource<Unit> = try {
        bodyMeasurementDao.getMeasurementsByUser(auth.currentUser?.uid ?: "")
            .first()
            .find { it.id == measurementId }
            ?.let { bodyMeasurementDao.deleteMeasurement(it) }
        Resource.Success(Unit)
    } catch (e: Exception) { Resource.Error(e.message ?: "Eroare", e) }

    override suspend fun addProgressPhoto(
        userId: String, photoUri: String, angle: PhotoAngle, notes: String
    ): Resource<ProgressPhoto> = try {
        val photoId = generateUid()
        val storageRef = storage.reference.child("progress_photos/$userId/$photoId.jpg")
        storageRef.putFile(Uri.parse(photoUri)).await()
        val downloadUrl = storageRef.downloadUrl.await().toString()

        val photo = ProgressPhoto(
            id = photoId, userId = userId, date = System.currentTimeMillis(),
            photoUrl = downloadUrl, angle = angle, notes = notes
        )
        progressPhotoDao.insertPhoto(photo.toEntity())
        Resource.Success(photo)
    } catch (e: Exception) { Resource.Error(e.message ?: "Eroare upload", e) }

    override suspend fun deleteProgressPhoto(photoId: String): Resource<Unit> = try {
        progressPhotoDao.getPhotosByUser(auth.currentUser?.uid ?: "")
            .first()
            .find { it.id == photoId }
            ?.let { progressPhotoDao.deletePhoto(it) }
        Resource.Success(Unit)
    } catch (e: Exception) { Resource.Error(e.message ?: "Eroare", e) }

    override suspend fun updateDailyStats(stats: DailyStats): Resource<Unit> = try {
        dailyStatsDao.insertStats(stats.toEntity())
        Resource.Success(Unit)
    } catch (e: Exception) { Resource.Error(e.message ?: "Eroare", e) }

    private suspend fun syncMeasurementToFirestore(measurement: BodyMeasurement) {
        try {
            auth.currentUser?.uid?.let { uid ->
                firestore.collection(FirestoreCollections.USERS).document(uid)
                    .collection(FirestoreCollections.BODY_MEASUREMENTS).document(measurement.id).set(measurement).await()
            }
        } catch (e: Exception) { Timber.w(e, "Sync failed") }
    }

    private fun BodyMeasurementEntity.toDomain() = BodyMeasurement(
        id = id, userId = userId, date = date, weightKg = weightKg,
        bodyFatPercent = bodyFatPercent, muscleMassKg = muscleMassKg, bmi = bmi,
        chestCm = chestCm, waistCm = waistCm, hipsCm = hipsCm,
        leftArmCm = leftArmCm, rightArmCm = rightArmCm,
        leftThighCm = leftThighCm, rightThighCm = rightThighCm,
        leftCalfCm = leftCalfCm, rightCalfCm = rightCalfCm, notes = notes
    )

    private fun BodyMeasurement.toEntity() = BodyMeasurementEntity(
        id = id.ifEmpty { generateUid() }, userId = userId, date = date,
        weightKg = weightKg, bodyFatPercent = bodyFatPercent, muscleMassKg = muscleMassKg,
        bmi = bmi, chestCm = chestCm, waistCm = waistCm, hipsCm = hipsCm,
        leftArmCm = leftArmCm, rightArmCm = rightArmCm,
        leftThighCm = leftThighCm, rightThighCm = rightThighCm,
        leftCalfCm = leftCalfCm, rightCalfCm = rightCalfCm, notes = notes
    )

    private fun ProgressPhotoEntity.toDomain() = ProgressPhoto(
        id = id, userId = userId, date = date, photoUrl = photoUrl,
        thumbnailUrl = thumbnailUrl, angle = angle, notes = notes
    )

    private fun ProgressPhoto.toEntity() = ProgressPhotoEntity(
        id = id, userId = userId, date = date, photoUrl = photoUrl,
        thumbnailUrl = thumbnailUrl, angle = angle, notes = notes
    )

    private fun PersonalRecordEntity.toDomain() = PersonalRecord(
        id = id, userId = userId, exerciseId = exerciseId, exerciseName = exerciseName,
        value = value, unit = unit, recordType = recordType, achievedAt = achievedAt
    )

    private fun DailyStatsEntity.toDomain() = DailyStats(
        date = date, userId = userId, steps = steps, caloriesBurned = caloriesBurned,
        caloriesConsumed = caloriesConsumed, activeMinutes = activeMinutes,
        workoutCompleted = workoutCompleted, waterMl = waterMl,
        sleepHours = sleepHours, restingHeartRate = restingHeartRate
    )

    private fun DailyStats.toEntity() = DailyStatsEntity(
        date = date.startOfDay(), userId = userId, steps = steps,
        caloriesBurned = caloriesBurned, caloriesConsumed = caloriesConsumed,
        activeMinutes = activeMinutes, workoutCompleted = workoutCompleted,
        waterMl = waterMl, sleepHours = sleepHours, restingHeartRate = restingHeartRate
    )
}
