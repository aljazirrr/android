package com.fitlife.app.features.exercises.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitlife.app.core.domain.model.Exercise
import com.fitlife.app.core.ui.components.*
import com.fitlife.app.core.ui.theme.FitGreen
import com.fitlife.app.core.utils.Equipment
import com.fitlife.app.core.utils.ExerciseType
import com.fitlife.app.core.utils.MuscleGroup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseListScreen(
    onExerciseClick: (String) -> Unit,
    onAddCustomExercise: () -> Unit,
    viewModel: ExerciseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showFilters by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // ─── Top Bar ──────────────────────────────────────────────────────────
        TopAppBar(
            title = { Text("Bibliotecă Exerciții", fontWeight = FontWeight.Bold) },
            actions = {
                IconButton(onClick = { showFilters = !showFilters }) {
                    Icon(Icons.Default.FilterList, "Filtre", tint = if (uiState.hasActiveFilters) FitGreen else LocalContentColor.current)
                }
                IconButton(onClick = onAddCustomExercise) {
                    Icon(Icons.Default.Add, "Adaugă exercițiu")
                }
            }
        )

        // ─── Search Bar ───────────────────────────────────────────────────────
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = viewModel::onSearchChange,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Caută exerciții...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                if (uiState.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onSearchChange("") }) {
                        Icon(Icons.Default.Clear, null)
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FitGreen)
        )

        // ─── Filters ──────────────────────────────────────────────────────────
        AnimatedVisibility(visible = showFilters) {
            FilterPanel(
                selectedMuscle = uiState.selectedMuscleGroup,
                selectedEquipment = uiState.selectedEquipment,
                selectedType = uiState.selectedType,
                onMuscleSelect = viewModel::onMuscleGroupFilter,
                onEquipmentSelect = viewModel::onEquipmentFilter,
                onTypeSelect = viewModel::onTypeFilter,
                onClearFilters = viewModel::clearFilters
            )
        }

        // ─── Muscle Group Chips ───────────────────────────────────────────────
        LazyRow(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = uiState.selectedMuscleGroup == null,
                    onClick = { viewModel.onMuscleGroupFilter(null) },
                    label = { Text("Toate") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = FitGreen,
                        selectedLabelColor = androidx.compose.ui.graphics.Color.White
                    )
                )
            }
            items(MuscleGroup.values().toList()) { muscle ->
                FilterChip(
                    selected = uiState.selectedMuscleGroup == muscle,
                    onClick = { viewModel.onMuscleGroupFilter(muscle) },
                    label = { Text(muscle.displayName()) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = FitGreen,
                        selectedLabelColor = androidx.compose.ui.graphics.Color.White
                    )
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // ─── Exercise Grid ────────────────────────────────────────────────────
        if (uiState.filteredExercises.isEmpty()) {
            EmptyState(
                icon = Icons.Default.FitnessCenter,
                title = "Niciun exercițiu găsit",
                description = if (uiState.searchQuery.isNotEmpty())
                    "Încearcă un termen diferit" else "Adaugă propriul tău exercițiu",
                actionText = "Adaugă exercițiu",
                onAction = onAddCustomExercise
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.filteredExercises) { exercise ->
                    ExerciseGridItem(
                        exercise = exercise,
                        onClick = { onExerciseClick(exercise.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ExerciseGridItem(exercise: Exercise, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .padding(bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(exercise.muscleGroups.firstOrNull()?.emoji() ?: "💪", style = MaterialTheme.typography.headlineMedium)
            }
            Spacer(Modifier.height(8.dp))
            Text(exercise.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, maxLines = 2)
            Spacer(Modifier.height(4.dp))
            Text(
                exercise.muscleGroups.firstOrNull()?.displayName() ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = FitGreen
            )
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                DifficultyStars(difficulty = exercise.difficulty)
                Spacer(Modifier.weight(1f))
                AssistChip(
                    onClick = {},
                    label = { Text(exercise.equipment.displayName(), style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.height(20.dp)
                )
            }
        }
    }
}

@Composable
private fun DifficultyStars(difficulty: Int) {
    Row {
        repeat(3) { i ->
            Icon(
                if (i < difficulty) Icons.Default.Star else Icons.Default.StarBorder,
                null,
                modifier = Modifier.size(12.dp),
                tint = if (i < difficulty) FitGreen else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FilterPanel(
    selectedMuscle: MuscleGroup?,
    selectedEquipment: Equipment?,
    selectedType: ExerciseType?,
    onMuscleSelect: (MuscleGroup?) -> Unit,
    onEquipmentSelect: (Equipment?) -> Unit,
    onTypeSelect: (ExerciseType?) -> Unit,
    onClearFilters: () -> Unit
) {
    Card(modifier = Modifier.padding(16.dp), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Filtre", fontWeight = FontWeight.Bold)
                TextButton(onClick = onClearFilters) { Text("Șterge", color = FitGreen) }
            }
            Text("Tip exercițiu", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ExerciseType.values().toList()) { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { onTypeSelect(if (selectedType == type) null else type) },
                        label = { Text(type.displayName()) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = FitGreen, selectedLabelColor = androidx.compose.ui.graphics.Color.White)
                    )
                }
            }
            Text("Echipament", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(Equipment.values().toList()) { eq ->
                    FilterChip(
                        selected = selectedEquipment == eq,
                        onClick = { onEquipmentSelect(if (selectedEquipment == eq) null else eq) },
                        label = { Text(eq.displayName()) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = FitGreen, selectedLabelColor = androidx.compose.ui.graphics.Color.White)
                    )
                }
            }
        }
    }
}

// Extension display names
fun MuscleGroup.displayName() = when (this) {
    MuscleGroup.CHEST -> "Piept"
    MuscleGroup.BACK -> "Spate"
    MuscleGroup.SHOULDERS -> "Umeri"
    MuscleGroup.BICEPS -> "Biceps"
    MuscleGroup.TRICEPS -> "Triceps"
    MuscleGroup.FOREARMS -> "Antebrațe"
    MuscleGroup.CORE -> "Core"
    MuscleGroup.QUADRICEPS -> "Cvadriceps"
    MuscleGroup.HAMSTRINGS -> "Ischio"
    MuscleGroup.GLUTES -> "Fesieri"
    MuscleGroup.CALVES -> "Gambe"
    MuscleGroup.FULL_BODY -> "Corp întreg"
    MuscleGroup.CARDIO -> "Cardio"
}

fun MuscleGroup.emoji() = when (this) {
    MuscleGroup.CHEST -> "🏋️"
    MuscleGroup.BACK -> "🔙"
    MuscleGroup.SHOULDERS -> "💪"
    MuscleGroup.BICEPS -> "💪"
    MuscleGroup.TRICEPS -> "💪"
    MuscleGroup.FOREARMS -> "🤜"
    MuscleGroup.CORE -> "⚡"
    MuscleGroup.QUADRICEPS -> "🦵"
    MuscleGroup.HAMSTRINGS -> "🦵"
    MuscleGroup.GLUTES -> "🍑"
    MuscleGroup.CALVES -> "🦵"
    MuscleGroup.FULL_BODY -> "🏃"
    MuscleGroup.CARDIO -> "❤️"
}

fun Equipment.displayName() = when (this) {
    Equipment.NONE -> "Fără"
    Equipment.BARBELL -> "Bară"
    Equipment.DUMBBELL -> "Gantere"
    Equipment.KETTLEBELL -> "Kettlebell"
    Equipment.RESISTANCE_BAND -> "Elastic"
    Equipment.MACHINE -> "Aparat"
    Equipment.CABLE -> "Cablu"
    Equipment.BODYWEIGHT -> "Propriu"
    Equipment.PULL_UP_BAR -> "Bară"
    Equipment.BENCH -> "Bancă"
    Equipment.TREADMILL -> "Bandă"
    Equipment.BICYCLE -> "Bicicletă"
    Equipment.ROWING_MACHINE -> "Vâsle"
    Equipment.OTHER -> "Altele"
}

fun ExerciseType.displayName() = when (this) {
    ExerciseType.STRENGTH -> "Forță"
    ExerciseType.CARDIO -> "Cardio"
    ExerciseType.FLEXIBILITY -> "Flexibilitate"
    ExerciseType.BALANCE -> "Echilibru"
    ExerciseType.SPORTS -> "Sport"
    ExerciseType.OTHER -> "Altele"
}
