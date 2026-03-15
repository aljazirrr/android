package com.fitlife.app.features.profile.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fitlife.app.core.ui.theme.FitGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateBack: () -> Unit) {
    var darkMode by remember { mutableStateOf(true) }
    var notifications by remember { mutableStateOf(true) }
    var workoutReminders by remember { mutableStateOf(true) }
    var nutritionReminders by remember { mutableStateOf(false) }
    var metricUnits by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Setări", fontWeight = FontWeight.Bold) },
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

            // ─── Appearance ───────────────────────────────────────────────────
            SettingsGroupTitle("Aspect")
            SettingsCard {
                SettingsToggleItem(
                    icon = Icons.Default.DarkMode,
                    title = "Mod întunecat",
                    subtitle = "Temă dark pentru aplicație",
                    checked = darkMode,
                    onCheckedChange = { darkMode = it }
                )
            }

            // ─── Notifications ────────────────────────────────────────────────
            SettingsGroupTitle("Notificări")
            SettingsCard {
                SettingsToggleItem(
                    icon = Icons.Default.Notifications,
                    title = "Notificări active",
                    subtitle = "Activează toate notificările",
                    checked = notifications,
                    onCheckedChange = { notifications = it }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsToggleItem(
                    icon = Icons.Default.FitnessCenter,
                    title = "Remindere antrenament",
                    subtitle = "Notificare zilnică pentru workout",
                    checked = workoutReminders && notifications,
                    onCheckedChange = { workoutReminders = it }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsToggleItem(
                    icon = Icons.Default.Restaurant,
                    title = "Remindere nutriție",
                    subtitle = "Reamintire să loghezi mesele",
                    checked = nutritionReminders && notifications,
                    onCheckedChange = { nutritionReminders = it }
                )
            }

            // ─── Units ────────────────────────────────────────────────────────
            SettingsGroupTitle("Unități de măsură")
            SettingsCard {
                SettingsToggleItem(
                    icon = Icons.Default.Straighten,
                    title = "Sistem metric",
                    subtitle = if (metricUnits) "kg, cm, ml" else "lbs, inch, oz",
                    checked = metricUnits,
                    onCheckedChange = { metricUnits = it }
                )
            }

            // ─── Data ─────────────────────────────────────────────────────────
            SettingsGroupTitle("Date")
            SettingsCard {
                SettingsClickItem(
                    icon = Icons.Default.CloudDownload,
                    title = "Export date",
                    subtitle = "Exportă progresul în CSV"
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsClickItem(
                    icon = Icons.Default.DeleteForever,
                    title = "Șterge toate datele",
                    subtitle = "Acțiune ireversibilă",
                    tint = MaterialTheme.colorScheme.error
                )
            }

            // ─── App ──────────────────────────────────────────────────────────
            SettingsGroupTitle("Aplicație")
            SettingsCard {
                SettingsClickItem(
                    icon = Icons.Default.Info,
                    title = "Versiune",
                    subtitle = "FitLife v1.0.0"
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsClickItem(
                    icon = Icons.Default.Policy,
                    title = "Politică de confidențialitate",
                    subtitle = "Cum folosim datele tale"
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsClickItem(
                    icon = Icons.Default.Gavel,
                    title = "Termeni și condiții",
                    subtitle = "Acordul de utilizare"
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsGroupTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = FitGreen,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        content = { Column(content = content) }
    )
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
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
            colors = SwitchDefaults.colors(checkedThumbColor = FitGreen, checkedTrackColor = FitGreen.copy(alpha = 0.3f))
        )
    }
}

@Composable
private fun SettingsClickItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = tint)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
    }
}
