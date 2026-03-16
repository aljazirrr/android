package com.fitlife.app.features.workout.presentation.list

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitlife.app.core.domain.model.WorkoutSession
import com.fitlife.app.core.ui.components.EmptyState
import com.fitlife.app.core.ui.components.GradientButton
import com.fitlife.app.core.ui.components.SectionHeader
import com.fitlife.app.core.ui.theme.*
import com.fitlife.app.core.utils.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutListScreen(
    onStartWorkout: () -> Unit,
    onViewSession: (String) -> Unit,
    viewModel: WorkoutListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Istoric", "Statistici")

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Antrenamente", fontWeight = FontWeight.Bold) }
        )

        // ─── Start Workout Banner ─────────────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable(onClick = onStartWorkout),
            shape = RoundedCornerShape(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(FitGreen, Color(0xFF00BCD4))))
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Nou antrenament", style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Text("Începe o sesiune acum", style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f))
                    }
                    Icon(Icons.Default.PlayCircleFilled, null,
                        modifier = Modifier.size(48.dp), tint = Color.White)
                }
            }
        }

        // ─── Streak Stats ─────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WorkoutStatChip(
                label = "Total", value = "${uiState.streak.totalWorkouts}",
                icon = "🏋️", modifier = Modifier.weight(1f)
            )
            WorkoutStatChip(
                label = "Săptămâna", value = "${uiState.streak.thisWeekWorkouts}",
                icon = "📅", modifier = Modifier.weight(1f)
            )
            WorkoutStatChip(
                label = "Streak", value = "${uiState.streak.currentStreak} zile",
                icon = "🔥", modifier = Modifier.weight(1f)
            )
        }

        // ─── Tabs ─────────────────────────────────────────────────────────────
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
            0 -> HistoryTab(sessions = uiState.sessions, onViewSession = onViewSession, onStartWorkout = onStartWorkout)
            1 -> StatsTab(uiState = uiState)
        }
    }
}

@Composable
private fun WorkoutStatChip(label: String, value: String, icon: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 20.sp)
            Text(value, fontWeight = FontWeight.ExtraBold, color = FitGreen, style = MaterialTheme.typography.titleSmall)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun HistoryTab(
    sessions: List<WorkoutSession>,
    onViewSession: (String) -> Unit,
    onStartWorkout: () -> Unit
) {
    if (sessions.isEmpty()) {
        EmptyState(
            icon = Icons.Default.FitnessCenter,
            title = "Niciun antrenament încă",
            description = "Completează primul tău antrenament pentru a vedea istoricul",
            actionText = "Începe acum",
            onAction = onStartWorkout,
            modifier = Modifier.padding(32.dp)
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Group by date
            val grouped = sessions.groupBy { session ->
                session.completedAt?.toDateString() ?: session.scheduledDate.toDateString()
            }
            grouped.forEach { (date, daySessions) ->
                item {
                    Text(
                        text = date,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                items(daySessions) { session ->
                    WorkoutSessionCard(session = session, onClick = { onViewSession(session.id) })
                }
            }
        }
    }
}

@Composable
private fun WorkoutSessionCard(session: WorkoutSession, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp))
                        .background(FitGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.FitnessCenter, null, tint = FitGreen, modifier = Modifier.size(24.dp))
                }
                Column {
                    Text(session.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "${session.exercises.size} exerciții",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (session.caloriesBurned > 0) {
                            Text("•", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${session.caloriesBurned} kcal",
                                style = MaterialTheme.typography.bodySmall, color = FitOrange)
                        }
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    session.durationSeconds.secondsToFormattedTime(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = FitGreen
                )
                // Rating stars
                if (session.rating > 0) {
                    Row {
                        repeat(session.rating) {
                            Text("⭐", fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsTab(uiState: WorkoutListUiState) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Statistici generale", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        StatItem("Total antrenamente", "${uiState.streak.totalWorkouts}", FitGreen)
                        StatItem("Streak maxim", "${uiState.streak.longestStreak} zile", FitOrange)
                        StatItem("Luna aceasta", "${uiState.streak.thisMonthWorkouts}", FitBlue)
                    }

                    HorizontalDivider()

                    if (uiState.sessions.isNotEmpty()) {
                        val avgDuration = uiState.sessions
                            .filter { it.durationSeconds > 0 }
                            .map { it.durationSeconds }
                            .takeIf { it.isNotEmpty() }
                            ?.average()?.toInt() ?: 0

                        val totalCalories = uiState.sessions.sumOf { it.caloriesBurned }
                        val totalVolume = uiState.sessions.sumOf { it.totalVolume.toDouble() }.toFloat()

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            StatItem("Durată medie", avgDuration.secondsToFormattedTime(), FitPurple)
                            StatItem("Calorii totale", "$totalCalories", FitRed)
                            StatItem("Volum total", "${totalVolume.toInt()} kg", FitGreen)
                        }
                    }
                }
            }
        }

        // Weekly activity heatmap
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Activitate săptămânală", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    val days = listOf("L", "M", "M", "J", "V", "S", "D")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        days.forEach { day ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp))
                                        .background(FitGreen.copy(alpha = (0.1f + Math.random() * 0.8f).toFloat())),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(day, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.ExtraBold, color = color, fontSize = 18.sp)
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
