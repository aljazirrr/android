package com.radiowave.app.features.schedule.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.radiowave.app.core.domain.model.ScheduledRecording
import com.radiowave.app.core.ui.theme.SchedulePurple
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Schedule,
                            null,
                            tint = SchedulePurple,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Scheduled Recordings", fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showAddDialog() }) {
                        Icon(Icons.Default.Add, "Add schedule")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddDialog() },
                containerColor = SchedulePurple
            ) {
                Icon(Icons.Default.Add, "Add schedule", tint = androidx.compose.ui.graphics.Color.White)
            }
        }
    ) { padding ->
        if (uiState.schedules.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.AlarmAdd,
                        null,
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No scheduled recordings",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Tap + to schedule a recording",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 100.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.schedules, key = { it.id }) { schedule ->
                    ScheduleItem(
                        schedule = schedule,
                        onToggle = { viewModel.toggleEnabled(schedule.id, !schedule.isEnabled) },
                        onCancel = { viewModel.cancelSchedule(schedule) }
                    )
                }
            }
        }
    }

    if (uiState.showAddDialog) {
        AddScheduleDialog(
            onConfirm = { stationName, url, date, duration ->
                viewModel.addSchedule(
                    stationUuid = "",
                    stationName = stationName,
                    stationUrl = url,
                    scheduledAt = date,
                    durationMinutes = duration
                )
            },
            onDismiss = { viewModel.dismissAddDialog() }
        )
    }
}

@Composable
private fun ScheduleItem(
    schedule: ScheduledRecording,
    onToggle: () -> Unit,
    onCancel: () -> Unit
) {
    val dateFormat = SimpleDateFormat("EEE, MMM dd • HH:mm", Locale.getDefault())
    val isPast = schedule.scheduledAt.before(Date())

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (schedule.isEnabled && !isPast)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Schedule,
                null,
                tint = if (schedule.isEnabled && !isPast) SchedulePurple
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    schedule.stationName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    dateFormat.format(schedule.scheduledAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isPast) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    "${schedule.durationMinutes} minutes",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                if (isPast) {
                    Text(
                        "Completed",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            Switch(
                checked = schedule.isEnabled,
                onCheckedChange = { onToggle() },
                enabled = !isPast
            )
            IconButton(onClick = onCancel) {
                Icon(
                    Icons.Default.DeleteOutline,
                    "Delete",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun AddScheduleDialog(
    onConfirm: (String, String, Date, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var stationName by remember { mutableStateOf("") }
    var stationUrl by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("30") }
    var hour by remember { mutableStateOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY).toString()) }
    var minute by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MINUTE).toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule Recording") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = stationName,
                    onValueChange = { stationName = it },
                    label = { Text("Station name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = stationUrl,
                    onValueChange = { stationUrl = it },
                    label = { Text("Stream URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = hour,
                        onValueChange = { hour = it },
                        label = { Text("Hour (0-23)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = minute,
                        onValueChange = { minute = it },
                        label = { Text("Minute") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Duration (minutes)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val calendar = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, hour.toIntOrNull() ?: 12)
                        set(Calendar.MINUTE, minute.toIntOrNull() ?: 0)
                        set(Calendar.SECOND, 0)
                        if (timeInMillis < System.currentTimeMillis()) {
                            add(Calendar.DAY_OF_MONTH, 1)
                        }
                    }
                    onConfirm(
                        stationName,
                        stationUrl,
                        calendar.time,
                        duration.toIntOrNull() ?: 30
                    )
                },
                enabled = stationName.isNotBlank()
            ) { Text("Schedule") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
