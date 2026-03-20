package com.radiowave.app.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.radiowave.app.core.data.local.dao.*
import com.radiowave.app.core.data.local.entities.*

@Database(
    entities = [
        FavoriteStationEntity::class,
        RecordingEntity::class,
        ScheduledRecordingEntity::class,
        ListeningHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class RadioDatabase : RoomDatabase() {
    abstract fun favoriteStationDao(): FavoriteStationDao
    abstract fun recordingDao(): RecordingDao
    abstract fun scheduledRecordingDao(): ScheduledRecordingDao
    abstract fun listeningHistoryDao(): ListeningHistoryDao
}
