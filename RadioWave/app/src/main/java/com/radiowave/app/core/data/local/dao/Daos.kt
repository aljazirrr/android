package com.radiowave.app.core.data.local.dao

import androidx.room.*
import com.radiowave.app.core.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteStationDao {
    @Query("SELECT * FROM favorite_stations ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteStationEntity>>

    @Query("SELECT stationUuid FROM favorite_stations")
    fun getAllFavoriteIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(station: FavoriteStationEntity)

    @Query("DELETE FROM favorite_stations WHERE stationUuid = :uuid")
    suspend fun deleteFavorite(uuid: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_stations WHERE stationUuid = :uuid)")
    suspend fun isFavorite(uuid: String): Boolean

    @Query("SELECT COUNT(*) FROM favorite_stations")
    suspend fun getFavoriteCount(): Int
}

@Dao
interface RecordingDao {
    @Query("SELECT * FROM recordings ORDER BY createdAt DESC")
    fun getAllRecordings(): Flow<List<RecordingEntity>>

    @Query("SELECT * FROM recordings WHERE stationUuid = :uuid ORDER BY createdAt DESC")
    fun getRecordingsByStation(uuid: String): Flow<List<RecordingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecording(recording: RecordingEntity): Long

    @Query("UPDATE recordings SET title = :newTitle WHERE id = :id")
    suspend fun renameRecording(id: Long, newTitle: String)

    @Query("UPDATE recordings SET cloudUrl = :cloudUrl, isUploaded = 1 WHERE id = :id")
    suspend fun updateCloudUrl(id: Long, cloudUrl: String)

    @Query("DELETE FROM recordings WHERE id = :id")
    suspend fun deleteRecording(id: Long)

    @Query("SELECT SUM(sizeBytes) FROM recordings")
    suspend fun getTotalSize(): Long?

    @Query("SELECT COUNT(*) FROM recordings")
    suspend fun getRecordingCount(): Int
}

@Dao
interface ScheduledRecordingDao {
    @Query("SELECT * FROM scheduled_recordings ORDER BY scheduledAt ASC")
    fun getAllScheduled(): Flow<List<ScheduledRecordingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduled(scheduled: ScheduledRecordingEntity): Long

    @Query("UPDATE scheduled_recordings SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun setEnabled(id: Long, isEnabled: Boolean)

    @Query("UPDATE scheduled_recordings SET workerId = :workerId WHERE id = :id")
    suspend fun updateWorkerId(id: Long, workerId: String)

    @Query("DELETE FROM scheduled_recordings WHERE id = :id")
    suspend fun deleteScheduled(id: Long)

    @Query("SELECT * FROM scheduled_recordings WHERE isEnabled = 1 AND scheduledAt > :now ORDER BY scheduledAt ASC")
    suspend fun getUpcomingSchedules(now: Long): List<ScheduledRecordingEntity>
}

@Dao
interface ListeningHistoryDao {
    @Query("SELECT * FROM listening_history ORDER BY listenedAt DESC LIMIT :limit")
    fun getHistory(limit: Int = 50): Flow<List<ListeningHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: ListeningHistoryEntity)

    @Query("""
        SELECT stationUuid, stationName, stationFavicon, COUNT(*) as playCount, SUM(durationMs) as totalDuration
        FROM listening_history
        WHERE listenedAt > :since
        GROUP BY stationUuid
        ORDER BY playCount DESC
        LIMIT :limit
    """)
    suspend fun getTopStations(since: Long, limit: Int = 10): List<TopStationResult>

    @Query("SELECT DISTINCT stationUuid FROM listening_history ORDER BY listenedAt DESC LIMIT :limit")
    suspend fun getRecentlyListenedUuids(limit: Int = 20): List<String>

    @Query("DELETE FROM listening_history WHERE listenedAt < :before")
    suspend fun deleteOldHistory(before: Long)

    @Query("DELETE FROM listening_history")
    suspend fun clearHistory()
}

data class TopStationResult(
    val stationUuid: String,
    val stationName: String,
    val stationFavicon: String,
    val playCount: Int,
    val totalDuration: Long
)
