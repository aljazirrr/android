package com.fooddiary.app.ui.screens.diary

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fooddiary.app.domain.model.FoodEntry
import com.fooddiary.app.domain.model.MealType
import com.fooddiary.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(
    onAddFood: (Long, MealType) -> Unit,
    onNavigateToSummary: () -> Unit,
    viewModel: DiaryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showNotesDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Jurnal Alimentar", fontWeight = FontWeight.Bold) },
            actions = {
                IconButton(onClick = onNavigateToSummary) {
                    Icon(Icons.Default.BarChart, contentDescription = "Rezumat săptămânal")
                }
            }
        )

        LazyColumn(
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Date selector
            item {
                DateSelector(
                    date = uiState.selectedDate,
                    onPrevious = viewModel::previousDay,
                    onNext = viewModel::nextDay
                )
            }

            // Daily totals card
            item { DailyTotalsCard(uiState) }

            // Macro breakdown
            item { MacroCard(uiState) }

            // Water
            item { WaterCard(uiState.waterMl, onAdd = viewModel::addWater) }

            // Meals
            items(MealType.entries) { mealType ->
                val mealEntries = uiState.entries.filter { it.mealType == mealType }
                MealCard(
                    mealType = mealType,
                    entries = mealEntries,
                    onAddFood = { onAddFood(uiState.selectedDate, mealType) },
                    onRemoveEntry = viewModel::removeEntry
                )
            }

            // Notes
            item {
                NotesCard(
                    notes = uiState.dailyLog?.notes ?: "",
                    onClick = { showNotesDialog = true }
                )
            }
        }
    }

    if (showNotesDialog) {
        NotesDialog(
            currentNotes = uiState.dailyLog?.notes ?: "",
            onDismiss = { showNotesDialog = false },
            onSave = { notes ->
                viewModel.updateNotes(notes)
                showNotesDialog = false
            }
        )
    }
}

// ─── Date Selector ───────────────────────────────────────────────────────────

@Composable
private fun DateSelector(date: Long, onPrevious: () -> Unit, onNext: () -> Unit) {
    val isToday = date.isToday()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Default.ChevronLeft, null)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if (isToday) "Astăzi" else date.toDateString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                date.toDateString("EEEE, d MMMM"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onNext, enabled = !isToday) {
            Icon(
                Icons.Default.ChevronRight, null,
                tint = if (isToday) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                else LocalContentColor.current
            )
        }
    }
}

// ─── Daily Totals ────────────────────────────────────────────────────────────

@Composable
private fun DailyTotalsCard(state: DiaryUiState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "${state.totalCalories}",
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "kcal consumate",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─── Macros ──────────────────────────────────────────────────────────────────

@Composable
private fun MacroCard(state: DiaryUiState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Macronutrienți",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MacroItem("Proteine", "${state.totalProteinG.toInt()}g", Green)
                MacroItem("Carbohidrați", "${state.totalCarbsG.toInt()}g", Blue)
                MacroItem("Grăsimi", "${state.totalFatG.toInt()}g", Orange)
                MacroItem("Fibre", "${state.totalFiberG.toInt()}g", Purple)
            }
            if (state.totalSugarG > 0) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Zahăr", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${state.totalSugarG.toInt()}g", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun MacroItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, color = color, fontSize = 18.sp)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ─── Water ───────────────────────────────────────────────────────────────────

@Composable
private fun WaterCard(waterMl: Int, onAdd: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Blue.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("💧", fontSize = 24.sp)
                Column {
                    Text("Apă", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("${waterMl}ml", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            FilledIconButton(
                onClick = { onAdd() },
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Blue)
            ) {
                Icon(Icons.Default.Add, null, tint = Color.White)
            }
        }
    }
}

// ─── Meal Card ───────────────────────────────────────────────────────────────

@Composable
private fun MealCard(
    mealType: MealType,
    entries: List<FoodEntry>,
    onAddFood: () -> Unit,
    onRemoveEntry: (String) -> Unit
) {
    val totalCal = entries.sumOf { it.calories }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        when (mealType) {
                            MealType.BREAKFAST -> "🌅"
                            MealType.LUNCH -> "☀️"
                            MealType.DINNER -> "🌙"
                            MealType.SNACK -> "🍪"
                        },
                        fontSize = 20.sp
                    )
                    Text(mealType.displayName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (totalCal > 0) {
                        Text("$totalCal kcal", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Green)
                    }
                    IconButton(onClick = onAddFood, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                }
            }

            if (entries.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                entries.forEach { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(entry.foodName, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "${entry.quantityGrams.toInt()}g • ${entry.proteinG.toInt()}P • ${entry.carbsG.toInt()}C • ${entry.fatG.toInt()}G",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${entry.calories} kcal", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { onRemoveEntry(entry.id) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            } else {
                Text(
                    "Apasă + pentru a adăuga",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

// ─── Notes ───────────────────────────────────────────────────────────────────

@Composable
private fun NotesCard(notes: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.EditNote, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                if (notes.isBlank()) "Adaugă notițe pentru ziua aceasta..." else notes,
                style = MaterialTheme.typography.bodyMedium,
                color = if (notes.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                else MaterialTheme.colorScheme.onSurface,
                maxLines = 3
            )
        }
    }
}

@Composable
private fun NotesDialog(currentNotes: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var text by remember { mutableStateOf(currentNotes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Notițe") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Cum te-ai simțit astăzi? Ce ai observat?") },
                minLines = 3,
                maxLines = 6
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(text) }) { Text("Salvează") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Anulează") }
        }
    )
}
