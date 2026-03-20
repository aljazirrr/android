package com.radiowave.app.features.schedule.data

import android.content.Context
import androidx.work.WorkManager
import com.radiowave.app.core.data.local.dao.ScheduledRecordingDao
import com.radiowave.app.core.data.local.entities.ScheduledRecordingEntity
import com.radiowave.app.core.domain.model.ScheduledRecording
import com.radiowave.app.features.schedule.domain.ScheduleRepository
import com.radiowave.app.features.schedule.worker.RecordingWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject

class ScheduleRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: ScheduledRecordingDao
) : ScheduleRepository {

    private val workManager = WorkManager.getInstance(context)

    override fun getScheduledRecordings(): Flow<List<ScheduledRecording>> =
        dao.getAllScheduled().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun scheduleRecording(scheduled: ScheduledRecording): Long {
        val id = dao.insertScheduled(
            ScheduledRecordingEntity(
                stationUuid = scheduled.stationUuid,
                stationName = scheduled.stationName,
                stationUrl = scheduled.stationUrl,
                title = scheduled.title,
                scheduledAt = scheduled.scheduledAt.time,
                durationMinutes = scheduled.durationMinutes,
                isEnabled = scheduled.isEnabled
            )
        )

        val delayMs = scheduled.scheduledAt.time - System.currentTimeMillis()
        if (delayMs > 0) {
            val workRequest = RecordingWorker.buildWorkRequest(
                stationName = scheduled.stationName,
                stationUrl = scheduled.stationUrl,
                durationMinutes = scheduled.durationMinutes,
                delayMs = delayMs
            )
            workManager.enqueue(workRequest)
            dao.updateWorkerId(id, workRequest.id.toString())
        }
        return id
    }

    override suspend fun cancelSchedule(id: Long) {
        dao.deleteScheduled(id)
    }

    override suspend fun setEnabled(id: Long, enabled: Boolean) {
        dao.setEnabled(id, enabled)
    }

    private fun ScheduledRecordingEntity.toDomain() = ScheduledRecording(
        id = id,
        stationUuid = stationUuid,
        stationName = stationName,
        stationUrl = stationUrl,
        title = title,
        scheduledAt = Date(scheduledAt),
        durationMinutes = durationMinutes,
        isEnabled = isEnabled,
        workerId = workerId
    )
}
