package com.fitlife.app.features.nutrition.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitlife.app.core.domain.model.Food
import com.fitlife.app.core.domain.model.FoodEntry
import com.fitlife.app.core.ui.components.GradientButton
import com.fitlife.app.core.ui.theme.FitGreen
import com.fitlife.app.core.utils.MealType
import com.fitlife.app.core.utils.generateUid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodScreen(
    mealType: MealType,
    onNavigateBack: () -> Unit,
    viewModel: NutritionViewModel = hiltViewModel(),
    addFoodViewModel: AddFoodViewModel = hiltViewModel()
) {
    val uiState by addFoodViewModel.uiState.collectAsStateWithLifecycle()
    var selectedFood by remember { mutableStateOf<Food?>(null) }
    var quantity by remember { mutableStateOf("100") }
    var showAddDialog by remember { mutableStateOf(false) }

    val mealName = when (mealType) {
        MealType.BREAKFAST -> "Mic dejun"
        MealType.LUNCH -> "Prânz"
        MealType.DINNER -> "Cină"
        MealType.SNACK -> "Gustare"
        MealType.PRE_WORKOUT -> "Pre-antrenament"
        MealType.POST_WORKOUT -> "Post-antrenament"
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Adaugă la $mealName", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Înapoi")
                }
            }
        )

        // ─── Search ───────────────────────────────────────────────────────────
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = addFoodViewModel::onSearch,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Caută aliment...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                if (uiState.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { addFoodViewModel.onSearch("") }) {
                        Icon(Icons.Default.Clear, null)
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FitGreen)
        )

        // ─── Food List ────────────────────────────────────────────────────────
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = FitGreen)
            }
        } else if (uiState.foods.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.SearchOff, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    Spacer(Modifier.height(16.dp))
                    Text("Niciun aliment găsit", style = MaterialTheme.typography.titleMedium)
                    Text("Încearcă alt termen de căutare", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.foods) { food ->
                    FoodListItem(
                        food = food,
                        onClick = {
                            selectedFood = food
                            quantity = "100"
                            showAddDialog = true
                        }
                    )
                }
            }
        }
    }

    // ─── Add Food Dialog ──────────────────────────────────────────────────────
    if (showAddDialog && selectedFood != null) {
        val food = selectedFood!!
        val qty = quantity.toFloatOrNull() ?: 100f
        val calories = (food.caloriesPer100g * qty / 100).toInt()
        val protein = food.proteinPer100g * qty / 100
        val carbs = food.carbsPer100g * qty / 100
        val fat = food.fatPer100g * qty / 100

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(food.name, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Quantity input
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Cantitate (grame)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        trailingIcon = { Text("g", style = MaterialTheme.typography.bodyMedium) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FitGreen)
                    )

                    // Nutrition preview
                    Card(
                        colors = CardDefaults.cardColors(containerColor = FitGreen.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Valori pentru ${qty.toInt()}g:", style = MaterialTheme.typography.labelMedium, color = FitGreen)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                NutritionPreviewItem("Calorii", "$calories kcal")
                                NutritionPreviewItem("Proteine", "${String.format("%.1f", protein)}g")
                                NutritionPreviewItem("Carbohidrați", "${String.format("%.1f", carbs)}g")
                                NutritionPreviewItem("Grăsimi", "${String.format("%.1f", fat)}g")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                GradientButton(
                    text = "Adaugă",
                    onClick = {
                        val entry = FoodEntry(
                            id = generateUid(),
                            foodId = food.id,
                            foodName = food.name,
                            quantity = qty,
                            calories = calories,
                            proteinG = protein,
                            carbsG = carbs,
                            fatG = fat,
                            fiberG = food.fiberPer100g * qty / 100
                        )
                        viewModel.addFoodEntry(mealType, entry)
                        showAddDialog = false
                        onNavigateBack()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Anulează")
                }
            }
        )
    }
}

@Composable
private fun FoodListItem(food: Food, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(food.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                food.brand?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    "P: ${food.proteinPer100g}g  C: ${food.carbsPer100g}g  G: ${food.fatPer100g}g  /100g",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${food.caloriesPer100g}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = FitGreen
                )
                Text("kcal/100g", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun NutritionPreviewItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = FitGreen)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
