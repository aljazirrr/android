package com.radiowave.app.features.recording.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.radiowave.app.core.domain.model.RadioStation
import com.radiowave.app.core.ui.theme.*

@Composable
fun RecordingBottomSheet(
    station: RadioStation?,
    onDismiss: () -> Unit,
    viewModel: RecordingViewModel = hiltViewModel()
) {
    val isRecording by viewModel.isRecording.collectAsState()

    if (station == null) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Handle
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
        )

        Spacer(Modifier.height(24.dp))

        Text(
            "Record Station",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            station.name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(Modifier.height(32.dp))

        // Recording animation
        AnimatedVisibility(visible = isRecording) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                RecordingPulse()
                Spacer(Modifier.height(8.dp))
                Text(
                    "Recording...",
                    style = MaterialTheme.typography.labelLarge,
                    color = RecordingRed
                )
                Spacer(Modifier.height(24.dp))
            }
        }

        // Record button
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(if (isRecording) RecordingRed else MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = { viewModel.toggleRecording(station) },
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                    contentDescription = if (isRecording) "Stop recording" else "Start recording",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = if (isRecording) "Tap to stop" else "Tap to record",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(Modifier.height(24.dp))

        // Info cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoCard(
                icon = Icons.Default.Save,
                title = "Saved locally",
                subtitle = "On your device",
                modifier = Modifier.weight(1f)
            )
            InfoCard(
                icon = Icons.Default.Cloud,
                title = "Cloud backup",
                subtitle = "Upload anytime",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = onDismiss) {
            Text("Cancel")
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun RecordingPulse() {
    val infiniteTransition = rememberInfiniteTransition(label = "recording_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    Box(
        modifier = Modifier
            .size(20.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(RecordingRed)
    )
}

@Composable
private fun InfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}
