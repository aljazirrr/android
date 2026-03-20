package com.radiowave.app.features.archive.domain

import com.radiowave.app.core.domain.model.Recording
import kotlinx.coroutines.flow.Flow

interface ArchiveRepository {
    fun getRecordings(): Flow<List<Recording>>
    suspend fun renameRecording(id: Long, newTitle: String)
    suspend fun deleteRecording(id: Long)
    suspend fun getTotalSize(): Long
}
