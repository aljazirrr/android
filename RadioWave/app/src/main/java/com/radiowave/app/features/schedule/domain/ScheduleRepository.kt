package com.radiowave.app.features.schedule.domain

import com.radiowave.app.core.domain.model.ScheduledRecording
import kotlinx.coroutines.flow.Flow

interface ScheduleRepository {
    fun getScheduledRecordings(): Flow<List<ScheduledRecording>>
    suspend fun scheduleRecording(scheduled: ScheduledRecording): Long
    suspend fun cancelSchedule(id: Long)
    suspend fun setEnabled(id: Long, enabled: Boolean)
}
