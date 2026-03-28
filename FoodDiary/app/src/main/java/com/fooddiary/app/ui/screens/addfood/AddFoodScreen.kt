package com.fooddiary.app.ui.screens.addfood

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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fooddiary.app.domain.model.Food
import com.fooddiary.app.domain.model.MealType
import com.fooddiary.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodScreen(
    date: Long,
    mealType: MealType,
    onNavigateBack: () -> Unit,
    viewModel: AddFoodViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedFood by remember { mutableStateOf<Food?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Adaugă aliment - ${mealType.displayName}", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Înapoi")
                }
            }
        )

        // Search
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = viewModel::onSearch,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Caută aliment...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                if (uiState.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onSearch("") }) {
                        Icon(Icons.Default.Close, null)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        // Food list
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.foods) { food ->
                FoodItem(
                    food = food,
                    onClick = { selectedFood = food }
                )
            }

            if (uiState.foods.isEmpty() && !uiState.isLoading) {
                item {
                    Text(
                        "Niciun aliment găsit",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }

    // Quantity dialog
    selectedFood?.let { food ->
        QuantityDialog(
            food = food,
            onDismiss = { selectedFood = null },
            onConfirm = { quantity ->
                viewModel.addFood(date, mealType, food, quantity)
                selectedFood = null
                onNavigateBack()
            }
        )
    }
}

@Composable
private fun FoodItem(food: Food, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(food.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                if (food.brand != null) {
                    Text(food.brand, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    "${food.proteinPer100g.toInt()}P • ${food.carbsPer100g.toInt()}C • ${food.fatPer100g.toInt()}G  /100g",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                "${food.caloriesPer100g}\nkcal",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = Green,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun QuantityDialog(food: Food, onDismiss: () -> Unit, onConfirm: (Float) -> Unit) {
    var quantityText by remember { mutableStateOf("100") }
    val quantity = quantityText.toFloatOrNull() ?: 0f
    val factor = quantity / 100f

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(food.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Cantitate (grame)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        NutritionRow("Calorii", "${(food.caloriesPer100g * factor).toInt()} kcal", Green)
                        NutritionRow("Proteine", "${"%.1f".format(food.proteinPer100g * factor)}g", Green)
                        NutritionRow("Carbohidrați", "${"%.1f".format(food.carbsPer100g * factor)}g", Blue)
                        NutritionRow("Grăsimi", "${"%.1f".format(food.fatPer100g * factor)}g", Orange)
                        NutritionRow("Fibre", "${"%.1f".format(food.fiberPer100g * factor)}g", Purple)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(quantity) }, enabled = quantity > 0) {
                Text("Adaugă")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Anulează") }
        }
    )
}

@Composable
private fun NutritionRow(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = color)
    }
}
