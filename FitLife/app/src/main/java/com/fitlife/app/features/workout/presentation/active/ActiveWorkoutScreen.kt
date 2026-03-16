package com.fitlife.app.features.workout.presentation.active

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitlife.app.core.domain.model.CompletedExercise
import com.fitlife.app.core.domain.model.CompletedSet
import com.fitlife.app.core.ui.components.CircularProgressRing
import com.fitlife.app.core.ui.components.GradientButton
import com.fitlife.app.core.ui.theme.*
import com.fitlife.app.core.utils.secondsToFormattedTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    onWorkoutFinished: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ActiveWorkoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isFinished) {
        if (uiState.isFinished) onWorkoutFinished()
    }

    if (uiState.session == null) {
        QuickStartWorkout(onStart = { name ->
            viewModel.startWorkout(name)
        })
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ─── Top Bar ──────────────────────────────────────────────────────
            WorkoutTopBar(
                sessionName = uiState.session?.name ?: "",
                elapsedSeconds = uiState.elapsedSeconds,
                isRunning = uiState.isRunning,
                onToggleTimer = viewModel::toggleTimer,
                onFinish = viewModel::showFinishDialog,
                onBack = onNavigateBack
            )

            // ─── Rest Timer ───────────────────────────────────────────────────
            AnimatedVisibility(visible = uiState.isResting) {
                RestTimerBanner(
                    secondsRemaining = uiState.restTimerSeconds,
                    onSkip = viewModel::skipRest
                )
            }

            // ─── Exercise List ────────────────────────────────────────────────
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(uiState.session?.exercises ?: emptyList()) { exerciseIndex, exercise ->
                    ExerciseCard(
                        exercise = exercise,
                        exerciseIndex = exerciseIndex,
                        onSetComplete = { setIndex, reps, weight ->
                            viewModel.completeSet(exerciseIndex, setIndex, reps, weight)
                            viewModel.startRestTimer(60)
                        }
                    )
                }

                item {
                    OutlinedButton(
                        onClick = { /* Navigate to exercise picker */ },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, FitGreen)
                    ) {
                        Icon(Icons.Default.Add, null, tint = FitGreen)
                        Spacer(Modifier.width(8.dp))
                        Text("Adaugă exercițiu", color = FitGreen)
                    }
                }
            }
        }

        // ─── Finish Dialog ────────────────────────────────────────────────────
        if (uiState.showFinishDialog) {
            FinishWorkoutDialog(
                elapsedSeconds = uiState.elapsedSeconds,
                exerciseCount = uiState.session?.exercises?.size ?: 0,
                onFinish = { rating, notes -> viewModel.finishWorkout(rating, notes) },
                onDismiss = viewModel::hideFinishDialog
            )
        }
    }
}

@Composable
private fun QuickStartWorkout(onStart: (String) -> Unit) {
    var workoutName by remember { mutableStateOf("Antrenament nou") }
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.FitnessCenter, null, modifier = Modifier.size(80.dp), tint = FitGreen)
        Spacer(Modifier.height(24.dp))
        Text("Începe antrenamentul", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(32.dp))
        OutlinedTextField(
            value = workoutName,
            onValueChange = { workoutName = it },
            label = { Text("Nume antrenament") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(24.dp))
        GradientButton(
            text = "START",
            onClick = { onStart(workoutName) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun WorkoutTopBar(
    sessionName: String,
    elapsedSeconds: Int,
    isRunning: Boolean,
    onToggleTimer: () -> Unit,
    onFinish: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(FitGreen, Color(0xFF00BCD4))))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(sessionName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                Text(
                    elapsedSeconds.secondsToFormattedTime(),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
            Row {
                IconButton(onClick = onToggleTimer) {
                    Icon(
                        if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        null, tint = Color.White
                    )
                }
                IconButton(onClick = onFinish) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun RestTimerBanner(secondsRemaining: Int, onSkip: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(FitOrange.copy(alpha = 0.15f))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Timer, null, tint = FitOrange)
                Text("Pauză: ${secondsRemaining}s", fontWeight = FontWeight.Bold, color = FitOrange)
            }
            TextButton(onClick = onSkip) {
                Text("Sari", color = FitOrange)
            }
        }
    }
}

@Composable
private fun ExerciseCard(
    exercise: CompletedExercise,
    exerciseIndex: Int,
    onSetComplete: (setIndex: Int, reps: Int?, weight: Float?) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(exercise.exerciseName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                val completedCount = exercise.sets.count { it.isCompleted }
                Text(
                    "$completedCount/${exercise.sets.size}",
                    color = if (completedCount == exercise.sets.size) FitGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(8.dp))

            // Header row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Set", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(40.dp))
                Text("Reps", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(80.dp), textAlign = TextAlign.Center)
                Text("Kg", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(80.dp), textAlign = TextAlign.Center)
                Text("✓", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(40.dp), textAlign = TextAlign.Center)
            }

            exercise.sets.forEachIndexed { setIndex, set ->
                SetRow(
                    set = set,
                    setNumber = setIndex + 1,
                    onComplete = { reps, weight -> onSetComplete(setIndex, reps, weight) }
                )
            }
        }
    }
}

@Composable
private fun SetRow(set: CompletedSet, setNumber: Int, onComplete: (Int?, Float?) -> Unit) {
    var reps by remember { mutableStateOf(set.reps?.toString() ?: "") }
    var weight by remember { mutableStateOf(set.weightKg?.toString() ?: "") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (set.isCompleted) FitGreen.copy(alpha = 0.1f) else Color.Transparent)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "$setNumber",
            fontWeight = FontWeight.Bold,
            color = if (set.isCompleted) FitGreen else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(40.dp)
        )
        OutlinedTextField(
            value = reps,
            onValueChange = { reps = it.filter { c -> c.isDigit() } },
            modifier = Modifier.width(80.dp),
            textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Center, fontSize = 14.sp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = FitGreen,
                unfocusedBorderColor = if (set.isCompleted) FitGreen.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline
            ),
            enabled = !set.isCompleted
        )
        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it.filter { c -> c.isDigit() || c == '.' } },
            modifier = Modifier.width(80.dp),
            textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Center, fontSize = 14.sp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = FitGreen,
                unfocusedBorderColor = if (set.isCompleted) FitGreen.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline
            ),
            enabled = !set.isCompleted
        )
        Checkbox(
            checked = set.isCompleted,
            onCheckedChange = { checked ->
                if (checked) onComplete(reps.toIntOrNull(), weight.toFloatOrNull())
            },
            colors = CheckboxDefaults.colors(checkedColor = FitGreen),
            modifier = Modifier.size(40.dp)
        )
    }
}

@Composable
private fun FinishWorkoutDialog(
    elapsedSeconds: Int,
    exerciseCount: Int,
    onFinish: (Int, String) -> Unit,
    onDismiss: () -> Unit
) {
    var rating by remember { mutableStateOf(4) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Termină antrenamentul", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Summary
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(elapsedSeconds.secondsToFormattedTime(), fontWeight = FontWeight.ExtraBold, color = FitGreen, fontSize = 20.sp)
                        Text("Durată", style = MaterialTheme.typography.labelSmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$exerciseCount", fontWeight = FontWeight.ExtraBold, color = FitBlue, fontSize = 20.sp)
                        Text("Exerciții", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Text("Cum a fost antrenamentul?", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(5) { i ->
                        IconButton(onClick = { rating = i + 1 }, modifier = Modifier.size(36.dp)) {
                            Icon(
                                if (i < rating) Icons.Default.Star else Icons.Default.StarBorder,
                                null,
                                tint = FitOrange,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Note (opțional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            GradientButton(text = "Finalizează", onClick = { onFinish(rating, notes) }, modifier = Modifier.fillMaxWidth())
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Continuă") }
        }
    )
}
