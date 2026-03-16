package com.fitlife.app.features.profile.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fitlife.app.core.ui.theme.FitGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(onNavigateBack: () -> Unit) {
    var workoutReminders by remember { mutableStateOf(true) }
    var nutritionReminders by remember { mutableStateOf(true) }
    var progressUpdates by remember { mutableStateOf(false) }
    var weeklyReport by remember { mutableStateOf(true) }
    var achievementAlerts by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificări", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Înapoi")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            Text("Antrenamente", style = MaterialTheme.typography.labelLarge, color = FitGreen, fontWeight = FontWeight.Bold)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column {
                    NotificationToggleItem(
                        icon = Icons.Default.FitnessCenter,
                        title = "Reminder antrenament",
                        subtitle = "Amintește-ți să faci mișcare zilnic",
                        checked = workoutReminders,
                        onCheckedChange = { workoutReminders = it }
                    )
                }
            }

            Text("Nutriție", style = MaterialTheme.typography.labelLarge, color = FitGreen, fontWeight = FontWeight.Bold)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column {
                    NotificationToggleItem(
                        icon = Icons.Default.Restaurant,
                        title = "Reminder masă",
                        subtitle = "Notificări pentru mesele zilnice",
                        checked = nutritionReminders,
                        onCheckedChange = { nutritionReminders = it }
                    )
                }
            }

            Text("Progres & Rapoarte", style = MaterialTheme.typography.labelLarge, color = FitGreen, fontWeight = FontWeight.Bold)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column {
                    NotificationToggleItem(
                        icon = Icons.Default.TrendingUp,
                        title = "Actualizări progres",
                        subtitle = "Notificări despre evoluția ta",
                        checked = progressUpdates,
                        onCheckedChange = { progressUpdates = it }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    NotificationToggleItem(
                        icon = Icons.Default.Assessment,
                        title = "Raport săptămânal",
                        subtitle = "Rezumat al activității din săptămână",
                        checked = weeklyReport,
                        onCheckedChange = { weeklyReport = it }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    NotificationToggleItem(
                        icon = Icons.Default.EmojiEvents,
                        title = "Realizări",
                        subtitle = "Alertă când obții o nouă realizare",
                        checked = achievementAlerts,
                        onCheckedChange = { achievementAlerts = it }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun NotificationToggleItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, null, tint = FitGreen, modifier = Modifier.size(22.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = FitGreen, checkedTrackColor = FitGreen.copy(alpha = 0.4f))
        )
    }
}
