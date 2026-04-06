package com.bariatric.assistant.features.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bariatric.assistant.components.BariatricTextField
import com.bariatric.assistant.components.GradientButton
import com.bariatric.assistant.core.data.local.entity.MealType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FoodJournalScreen(
    viewModel: FoodJournalViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "🥗 Jurnal Alimentar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddDialog() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adaugă")
            }
        }
    ) { paddingValues ->
        if (uiState.entries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        Icons.Default.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Nicio masă înregistrată astăzi",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Apasă + pentru a adăuga",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                // Group by meal type
                val grouped = uiState.entries.groupBy { it.mealType }
                MealType.entries.forEach { mealType ->
                    val meals = grouped[mealType]
                    if (!meals.isNullOrEmpty()) {
                        item {
                            Text(
                                text = mealTypeToRomanian(mealType),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        items(meals, key = { it.id }) { entry ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(1.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = mealTypeEmoji(entry.mealType),
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = entry.foodName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = entry.portionDescription,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (!entry.notes.isNullOrBlank()) {
                                            Text(
                                                text = entry.notes,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                    Text(
                                        text = SimpleDateFormat("HH:mm", Locale.getDefault())
                                            .format(Date(entry.timestamp)),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    IconButton(
                                        onClick = { viewModel.removeEntry(entry) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Șterge",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Tip card
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                        )
                    ) {
                        Text(
                            text = "💡 Sfat: Mănâncă întotdeauna proteinele primele, apoi legumele, și la final carbohidrații. Mestecă fiecare îmbucătură de cel puțin 20 de ori.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        if (uiState.showAddDialog) {
            AddFoodDialog(
                foodName = uiState.foodName,
                onFoodNameChange = viewModel::updateFoodName,
                selectedMealType = uiState.selectedMealType,
                onMealTypeChange = viewModel::updateMealType,
                portionDescription = uiState.portionDescription,
                onPortionChange = viewModel::updatePortionDescription,
                notes = uiState.notes,
                onNotesChange = viewModel::updateNotes,
                onAdd = viewModel::addEntry,
                onDismiss = viewModel::hideAddDialog
            )
        }
    }
}

@Composable
private fun AddFoodDialog(
    foodName: String,
    onFoodNameChange: (String) -> Unit,
    selectedMealType: MealType,
    onMealTypeChange: (MealType) -> Unit,
    portionDescription: String,
    onPortionChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    onAdd: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Adaugă aliment",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                BariatricTextField(
                    value = foodName,
                    onValueChange = onFoodNameChange,
                    label = "Ce ai mâncat?",
                    placeholder = "Ex: Piept de pui fiert",
                    leadingIcon = Icons.Default.Restaurant
                )

                Text(
                    text = "Tipul mesei",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MealType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedMealType == type,
                            onClick = { onMealTypeChange(type) },
                            label = {
                                Text(
                                    text = mealTypeShort(type),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        )
                    }
                }

                BariatricTextField(
                    value = portionDescription,
                    onValueChange = onPortionChange,
                    label = "Porție",
                    placeholder = "Ex: 2 linguri, jumătate de cană"
                )

                BariatricTextField(
                    value = notes,
                    onValueChange = onNotesChange,
                    label = "Note (opțional)",
                    placeholder = "Ex: Am tolerat bine",
                    singleLine = false,
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            GradientButton(
                text = "Adaugă",
                onClick = onAdd,
                enabled = foodName.isNotBlank() && portionDescription.isNotBlank()
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anulează")
            }
        }
    )
}

private fun mealTypeToRomanian(type: MealType): String = when (type) {
    MealType.BREAKFAST -> "🌅 Mic Dejun"
    MealType.LUNCH -> "☀️ Prânz"
    MealType.DINNER -> "🌙 Cină"
    MealType.SNACK -> "🍎 Gustare"
}

private fun mealTypeShort(type: MealType): String = when (type) {
    MealType.BREAKFAST -> "Mic dejun"
    MealType.LUNCH -> "Prânz"
    MealType.DINNER -> "Cină"
    MealType.SNACK -> "Gustare"
}

private fun mealTypeEmoji(type: MealType): String = when (type) {
    MealType.BREAKFAST -> "🌅"
    MealType.LUNCH -> "☀️"
    MealType.DINNER -> "🌙"
    MealType.SNACK -> "🍎"
}
