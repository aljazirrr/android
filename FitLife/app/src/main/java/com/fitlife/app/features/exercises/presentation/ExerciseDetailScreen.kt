package com.fitlife.app.features.exercises.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitlife.app.core.domain.model.Exercise
import com.fitlife.app.core.ui.theme.FitGreen
import com.fitlife.app.core.ui.theme.FitOrange
import com.fitlife.app.core.utils.Equipment
import com.fitlife.app.core.utils.ExerciseType
import com.fitlife.app.core.utils.MuscleGroup
import com.fitlife.app.features.exercises.domain.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── ViewModel ────────────────────────────────────────────────────────────────

@HiltViewModel
class ExerciseDetailViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val _exercise = MutableStateFlow<Exercise?>(null)
    val exercise: StateFlow<Exercise?> = _exercise.asStateFlow()

    fun loadExercise(exerciseId: String) {
        viewModelScope.launch {
            _exercise.value = exerciseRepository.getExerciseById(exerciseId)
        }
    }
}

// ─── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    exerciseId: String,
    onNavigateBack: () -> Unit,
    onStartWorkout: () -> Unit = {},
    viewModel: ExerciseDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(exerciseId) { viewModel.loadExercise(exerciseId) }
    val exercise by viewModel.exercise.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(exercise?.name ?: "Exercițiu", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Înapoi")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onStartWorkout,
                containerColor = FitGreen,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.PlayArrow, null) },
                text = { Text("Antrenament nou", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        if (exercise == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = FitGreen)
            }
        } else {
            ExerciseDetailContent(
                exercise = exercise!!,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun ExerciseDetailContent(exercise: Exercise, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // ─── Hero ─────────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(FitGreen.copy(alpha = 0.3f), MaterialTheme.colorScheme.background)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                exercise.muscleGroups.firstOrNull()?.emoji() ?: "💪",
                fontSize = 80.sp
            )
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            // ─── Name & Type ──────────────────────────────────────────────────
            Text(exercise.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoChip(exercise.type.displayName(), FitGreen)
                InfoChip(exercise.equipment.displayName(), FitOrange)
                InfoChip("Dif: ${"★".repeat(exercise.difficulty)}${"☆".repeat(3 - exercise.difficulty.coerceAtMost(3))}", MaterialTheme.colorScheme.tertiary)
            }

            Spacer(Modifier.height(20.dp))

            // ─── Description ──────────────────────────────────────────────────
            if (exercise.description.isNotEmpty()) {
                SectionTitle("Descriere")
                Text(
                    exercise.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(20.dp))
            }

            // ─── Muscle Groups ────────────────────────────────────────────────
            if (exercise.muscleGroups.isNotEmpty()) {
                SectionTitle("Mușchi principali")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                    exercise.muscleGroups.forEach { muscle ->
                        MuscleChip(muscle, primary = true)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            if (exercise.secondaryMuscles.isNotEmpty()) {
                SectionTitle("Mușchi secundari")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                    exercise.secondaryMuscles.forEach { muscle ->
                        MuscleChip(muscle, primary = false)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // ─── Instructions ─────────────────────────────────────────────────
            if (exercise.instructions.isNotEmpty()) {
                SectionTitle("Instrucțiuni")
                Spacer(Modifier.height(8.dp))
                exercise.instructions.forEachIndexed { index, step ->
                    Row(
                        modifier = Modifier.padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(FitGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${index + 1}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Text(
                            step,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(100.dp)) // FAB padding
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
}

@Composable
private fun InfoChip(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            color = color,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MuscleChip(muscle: MuscleGroup, primary: Boolean) {
    val color = if (primary) FitGreen else FitOrange
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(muscle.emoji(), fontSize = 14.sp)
            Text(muscle.displayName(), color = color, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

