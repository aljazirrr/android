package com.fitlife.app.features.progress.presentation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitlife.app.core.domain.model.BodyMeasurement
import com.fitlife.app.core.ui.components.GradientButton
import com.fitlife.app.core.ui.components.SectionHeader
import com.fitlife.app.core.ui.theme.*
import com.fitlife.app.core.utils.toBmi
import com.fitlife.app.core.utils.toBmiCategory
import com.fitlife.app.core.utils.toDateString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    onAddMeasurement: () -> Unit,
    viewModel: ProgressViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Măsurători", "Fotografii", "Recorduri")

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Progres", fontWeight = FontWeight.Bold) },
            actions = {
                IconButton(onClick = onAddMeasurement) {
                    Icon(Icons.Default.Add, null)
                }
            }
        )

        // ─── Tab Row ──────────────────────────────────────────────────────────
        TabRow(selectedTabIndex = selectedTab, containerColor = MaterialTheme.colorScheme.surface) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) },
                    selectedContentColor = FitGreen
                )
            }
        }

        when (selectedTab) {
            0 -> MeasurementsTab(uiState = uiState, onAddMeasurement = onAddMeasurement)
            1 -> PhotosTab(uiState = uiState)
            2 -> RecordsTab(uiState = uiState)
        }
    }
}

@Composable
private fun MeasurementsTab(uiState: ProgressUiState, onAddMeasurement: () -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ─── Latest Stats ─────────────────────────────────────────────────────
        item {
            uiState.latestMeasurement?.let { m ->
                LatestMeasurementCard(measurement = m)
            } ?: EmptyMeasurementCard(onAddMeasurement = onAddMeasurement)
        }

        // ─── Weight History ───────────────────────────────────────────────────
        item {
            SectionHeader("Istoricul greutății")
        }

        if (uiState.measurements.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.MonitorWeight, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        Spacer(Modifier.height(8.dp))
                        Text("Nicio măsurătoare", style = MaterialTheme.typography.bodyLarge)
                        Text("Adaugă prima măsurătoare", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(16.dp))
                        GradientButton(text = "Adaugă", onClick = onAddMeasurement, modifier = Modifier.width(160.dp))
                    }
                }
            }
        } else {
            items(uiState.measurements.take(10)) { measurement ->
                MeasurementHistoryItem(measurement = measurement)
            }
        }
    }
}

@Composable
private fun LatestMeasurementCard(measurement: BodyMeasurement) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Ultima măsurătoare", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(measurement.date.toDateString(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                measurement.weightKg?.let { weight ->
                    MeasurementItem("Greutate", "${weight}kg", FitGreen)
                }
                measurement.bmi?.let { bmi ->
                    MeasurementItem("IMC", "$bmi\n${bmi.toBmiCategory()}", FitBlue)
                }
                measurement.bodyFatPercent?.let { fat ->
                    MeasurementItem("Grăsime", "${fat}%", FitOrange)
                }
            }
            if (measurement.waistCm != null || measurement.chestCm != null) {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    measurement.chestCm?.let { MeasurementItem("Piept", "${it}cm", FitPurple) }
                    measurement.waistCm?.let { MeasurementItem("Talie", "${it}cm", FitRed) }
                    measurement.hipsCm?.let { MeasurementItem("Șolduri", "${it}cm", FitOrangeLight) }
                }
            }
        }
    }
}

@Composable
private fun MeasurementItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.ExtraBold, color = color, fontSize = 18.sp, lineHeight = 22.sp)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EmptyMeasurementCard(onAddMeasurement: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onAddMeasurement),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = FitGreen.copy(alpha = 0.1f))
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(Icons.Default.Add, null, tint = FitGreen, modifier = Modifier.size(32.dp))
            Column {
                Text("Adaugă prima măsurătoare", fontWeight = FontWeight.Bold, color = FitGreen)
                Text("Urmărește-ți progresul în timp", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun MeasurementHistoryItem(measurement: BodyMeasurement) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(measurement.date.toDateString(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                measurement.notes.takeIf { it.isNotEmpty() }?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                measurement.weightKg?.let { Text("${it}kg", fontWeight = FontWeight.Bold, color = FitGreen) }
                measurement.bodyFatPercent?.let { Text("${it}%", fontWeight = FontWeight.Bold, color = FitOrange) }
            }
        }
    }
}

@Composable
private fun PhotosTab(uiState: ProgressUiState) {
    if (uiState.photos.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.PhotoCamera, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                Spacer(Modifier.height(16.dp))
                Text("Nicio fotografie de progres", style = MaterialTheme.typography.titleLarge)
                Text("Documentează-ți transformarea", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(uiState.photos) { photo ->
                Card(shape = RoundedCornerShape(16.dp)) {
                    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                            Icon(Icons.Default.Image, null, modifier = Modifier.align(Alignment.Center).size(32.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column {
                            Text(photo.date.toDateString(), fontWeight = FontWeight.Bold)
                            Text(photo.angle.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (photo.notes.isNotEmpty()) Text(photo.notes, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordsTab(uiState: ProgressUiState) {
    if (uiState.personalRecords.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🏆", fontSize = 64.sp)
                Spacer(Modifier.height(16.dp))
                Text("Niciun record personal", style = MaterialTheme.typography.titleLarge)
                Text("Completează antrenamente pentru a stabili recorduri", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(uiState.personalRecords) { record ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(record.exerciseName, fontWeight = FontWeight.Bold)
                            Text(record.recordType.name.replace("_", " "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("🏆", fontSize = 16.sp)
                            Text("${record.value} ${record.unit}", fontWeight = FontWeight.ExtraBold, color = FitGreen, fontSize = 18.sp)
                        }
                    }
                }
            }
        }
    }
}
