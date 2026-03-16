package com.fitlife.app.features.dashboard.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
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
import com.fitlife.app.core.ui.components.*
import com.fitlife.app.core.ui.theme.*
import com.fitlife.app.core.utils.*
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToWorkout: () -> Unit,
    onNavigateToNutrition: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToExercises: () -> Unit,
    onStartWorkout: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val user = uiState.user

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ─── Header ───────────────────────────────────────────────────────────
        item {
            DashboardHeader(
                userName = user?.displayName ?: "Atlet",
                streak = uiState.streak.currentStreak,
                onRefresh = viewModel::refresh
            )
        }

        // ─── Quick Stats ──────────────────────────────────────────────────────
        item {
            QuickStatsRow(
                uiState = uiState,
                onStepsClick = { onNavigateToProgress() },
                onCaloriesClick = { onNavigateToNutrition() },
                onWorkoutsClick = { onNavigateToWorkout() }
            )
        }

        // ─── Today's Calorie Ring ─────────────────────────────────────────────
        item {
            TodayCalorieCard(
                uiState = uiState,
                targetCalories = user?.dailyCalorieTarget ?: 2000,
                onNavigateToNutrition = onNavigateToNutrition
            )
        }

        // ─── Start Workout CTA ────────────────────────────────────────────────
        item {
            StartWorkoutCard(onStartWorkout = onStartWorkout)
        }

        // ─── Weekly Progress ──────────────────────────────────────────────────
        item {
            WeeklyProgressCard(
                weeklyWorkouts = uiState.weeklyWorkouts,
                weeklyTarget = user?.weeklyWorkoutTarget ?: 3
            )
        }

        // ─── Motivational Quote ───────────────────────────────────────────────
        item {
            MotivationalQuoteCard(quote = uiState.motivationalQuote)
        }

        // ─── Recent Workouts ──────────────────────────────────────────────────
        item {
            SectionHeader(
                title = "Antrenamente recente",
                actionText = "Vezi tot",
                onAction = onNavigateToWorkout,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        if (uiState.recentWorkouts.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Default.FitnessCenter,
                    title = "Niciun antrenament încă",
                    description = "Începe primul tău antrenament astăzi!",
                    actionText = "Începe acum",
                    onAction = onStartWorkout,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            items(uiState.recentWorkouts.take(3)) { session ->
                RecentWorkoutItem(
                    session = session,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // ─── Quick Actions ────────────────────────────────────────────────────
        item {
            SectionHeader(
                title = "Acțiuni rapide",
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item {
            QuickActionsGrid(
                onNavigateToExercises = onNavigateToExercises,
                onNavigateToNutrition = onNavigateToNutrition,
                onNavigateToProgress = onNavigateToProgress,
                onNavigateToWorkout = onNavigateToWorkout
            )
        }
    }
}

@Composable
private fun DashboardHeader(userName: String, streak: Int, onRefresh: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(FitGreen.copy(alpha = 0.15f), Color.Transparent)
                )
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                val greeting = when {
                    hour < 12 -> "Bună dimineața"
                    hour < 17 -> "Bună ziua"
                    else -> "Bună seara"
                }
                Text(
                    greeting,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    userName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (streak > 0) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = FitOrange.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔥", fontSize = 18.sp)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "$streak zile",
                                color = FitOrange,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reîmprospătare", tint = FitGreen)
                }
            }
        }
    }
}

@Composable
private fun QuickStatsRow(
    uiState: DashboardUiState,
    onStepsClick: () -> Unit,
    onCaloriesClick: () -> Unit,
    onWorkoutsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            title = "Pași",
            value = "${uiState.todayStats?.steps ?: 0}",
            icon = Icons.Default.DirectionsWalk,
            color = FitBlue,
            onClick = onStepsClick,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Calorii arse",
            value = "${uiState.todayStats?.caloriesBurned ?: 0}",
            unit = "kcal",
            icon = Icons.Default.LocalFireDepartment,
            color = FitOrange,
            onClick = onCaloriesClick,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Antrenamente",
            value = "${uiState.weeklyWorkouts.size}",
            unit = "săpt.",
            icon = Icons.Default.FitnessCenter,
            color = FitGreen,
            onClick = onWorkoutsClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TodayCalorieCard(
    uiState: DashboardUiState,
    targetCalories: Int,
    onNavigateToNutrition: () -> Unit
) {
    val consumed = uiState.todayNutrition?.totalCalories ?: 0
    val burned = uiState.todayStats?.caloriesBurned ?: 0
    val remaining = (targetCalories - consumed + burned).coerceAtLeast(0)
    val progress = (consumed.toFloat() / targetCalories).coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onNavigateToNutrition),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Calorii azi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressRing(
                    progress = progress,
                    size = 100.dp,
                    strokeWidth = 10.dp,
                    color = FitGreen
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "$consumed",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = FitGreen
                        )
                        Text("kcal", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MacroItem("Obiectiv", "$targetCalories kcal", FitBlue)
                    MacroItem("Arse", "$burned kcal", FitOrange)
                    MacroItem("Rămase", "$remaining kcal", FitGreen)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Macro breakdown
            if (uiState.todayNutrition != null) {
                val log = uiState.todayNutrition
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    MacroProgressItem("Proteine", log.totalProteinG.toInt(), 150, Color(0xFF4CAF50))
                    MacroProgressItem("Carbohidrați", log.totalCarbsG.toInt(), 250, Color(0xFF2196F3))
                    MacroProgressItem("Grăsimi", log.totalFatG.toInt(), 65, Color(0xFFFF9800))
                }
            }
        }
    }
}

@Composable
private fun MacroItem(label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun MacroProgressItem(name: String, current: Int, target: Int, color: Color) {
    val progress = (current.toFloat() / target).coerceIn(0f, 1f)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressRing(progress = progress, size = 52.dp, strokeWidth = 5.dp, color = color) {
            Text("${(progress * 100).toInt()}%", fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(4.dp))
        Text(name, style = MaterialTheme.typography.labelSmall)
        Text("${current}g", style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StartWorkoutCard(onStartWorkout: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onStartWorkout),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(listOf(FitGreen, Color(0xFF00BCD4)))
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Gata de antrenament?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        "Apasă pentru a începe sesiunea",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
                Icon(
                    Icons.Default.PlayCircleFilled,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun WeeklyProgressCard(weeklyWorkouts: List<WorkoutSession>, weeklyTarget: Int) {
    val daysOfWeek = listOf("L", "M", "M", "J", "V", "S", "D")
    val completedDays = weeklyWorkouts.mapNotNull { session ->
        session.completedAt?.let {
            val cal = Calendar.getInstance().apply { timeInMillis = it }
            cal.get(Calendar.DAY_OF_WEEK)
        }
    }.toSet()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Progres săptămânal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "${weeklyWorkouts.size}/$weeklyTarget",
                    color = FitGreen,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                daysOfWeek.forEachIndexed { index, day ->
                    val dayNum = index + 2 // Calendar.MONDAY = 2
                    val isCompleted = completedDays.contains(dayNum)
                    val isToday = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == dayNum
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isCompleted -> FitGreen
                                        isToday -> FitGreen.copy(alpha = 0.2f)
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCompleted) {
                                Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            } else {
                                Text(
                                    day,
                                    color = if (isToday) FitGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MotivationalQuoteCard(quote: MotivationalQuote) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(FitPurple.copy(alpha = 0.3f), FitBlue.copy(alpha = 0.3f))
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Text("💪", fontSize = 24.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "\"${quote.text}\"",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "— ${quote.author}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RecentWorkoutItem(session: WorkoutSession, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(FitGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.FitnessCenter, null, tint = FitGreen, modifier = Modifier.size(24.dp))
                }
                Column {
                    Text(session.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    Text(
                        session.completedAt?.toDateString() ?: session.scheduledDate.toDateString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    session.durationSeconds.secondsToFormattedTime(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = FitGreen
                )
                Text(
                    "${session.caloriesBurned} kcal",
                    style = MaterialTheme.typography.bodySmall,
                    color = FitOrange
                )
            }
        }
    }
}

@Composable
private fun QuickActionsGrid(
    onNavigateToExercises: () -> Unit,
    onNavigateToNutrition: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToWorkout: () -> Unit
) {
    val actions = listOf(
        Triple("Exerciții", Icons.Default.FitnessCenter, onNavigateToExercises),
        Triple("Nutriție", Icons.Default.Restaurant, onNavigateToNutrition),
        Triple("Progres", Icons.Default.TrendingUp, onNavigateToProgress),
        Triple("Antrenamente", Icons.Default.CalendarMonth, onNavigateToWorkout)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        actions.forEach { (title, icon, onClick) ->
            Card(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .clickable(onClick = onClick),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(icon, null, tint = FitGreen, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.height(4.dp))
                    Text(title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
