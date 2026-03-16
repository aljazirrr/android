package com.fitlife.app.features.progress.domain

import com.fitlife.app.core.domain.model.*
import com.fitlife.app.core.utils.Resource
import kotlinx.coroutines.flow.Flow

interface ProgressRepository {
    fun getMeasurementsByUser(userId: String): Flow<List<BodyMeasurement>>
    fun getLatestMeasurement(userId: String): Flow<BodyMeasurement?>
    fun getPhotosByUser(userId: String): Flow<List<ProgressPhoto>>
    fun getPersonalRecords(userId: String): Flow<List<PersonalRecord>>
    fun getStatsForDate(userId: String, date: Long): Flow<DailyStats?>
    fun getStatsInRange(userId: String, start: Long, end: Long): Flow<List<DailyStats>>
    suspend fun addMeasurement(measurement: BodyMeasurement): Resource<Unit>
    suspend fun deleteMeasurement(measurementId: String): Resource<Unit>
    suspend fun addProgressPhoto(userId: String, photoUri: String, angle: PhotoAngle, notes: String): Resource<ProgressPhoto>
    suspend fun deleteProgressPhoto(photoId: String): Resource<Unit>
    suspend fun updateDailyStats(stats: DailyStats): Resource<Unit>
}
