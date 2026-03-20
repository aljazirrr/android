package com.radiowave.app.features.archive.data

import com.radiowave.app.core.data.local.dao.RecordingDao
import com.radiowave.app.core.domain.model.Recording
import com.radiowave.app.features.archive.domain.ArchiveRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import java.util.Date
import javax.inject.Inject

class ArchiveRepositoryImpl @Inject constructor(
    private val recordingDao: RecordingDao
) : ArchiveRepository {

    override fun getRecordings(): Flow<List<Recording>> =
        recordingDao.getAllRecordings().map { entities ->
            entities.map { entity ->
                Recording(
                    id = entity.id,
                    stationUuid = entity.stationUuid,
                    stationName = entity.stationName,
                    title = entity.title,
                    filePath = entity.filePath,
                    cloudUrl = entity.cloudUrl,
                    durationMs = entity.durationMs,
                    sizeBytes = entity.sizeBytes,
                    createdAt = Date(entity.createdAt),
                    isUploaded = entity.isUploaded
                )
            }
        }

    override suspend fun renameRecording(id: Long, newTitle: String) {
        recordingDao.renameRecording(id, newTitle)
    }

    override suspend fun deleteRecording(id: Long) {
        // Note: we'd also need to delete the file from disk here
        recordingDao.deleteRecording(id)
    }

    override suspend fun getTotalSize(): Long =
        recordingDao.getTotalSize() ?: 0L
}
