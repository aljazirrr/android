package com.fitlife.app.features.nutrition.presentation

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitlife.app.core.domain.model.Meal
import com.fitlife.app.core.ui.components.CircularProgressRing
import com.fitlife.app.core.ui.components.GradientButton
import com.fitlife.app.core.ui.components.SectionHeader
import com.fitlife.app.core.ui.theme.*
import com.fitlife.app.core.utils.MealType
import com.fitlife.app.core.utils.toDateString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionScreen(
    onAddFood: (MealType) -> Unit,
    viewModel: NutritionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }

    Column(modifier = Modifier.fillMaxSize()) {
        // ─── Top Bar ──────────────────────────────────────────────────────────
        TopAppBar(title = { Text("Nutriție", fontWeight = FontWeight.Bold) })

        LazyColumn(
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ─── Date Selector ────────────────────────────────────────────────
            item {
                DateSelector(
                    selectedDate = selectedDate,
                    onDateChange = {
                        selectedDate = it
                        viewModel.loadForDate(it)
                    }
                )
            }

            // ─── Calorie Summary ──────────────────────────────────────────────
            item {
                CalorieSummaryCard(
                    consumed = uiState.todayLog?.totalCalories ?: 0,
                    target = uiState.calorieTarget,
                    burned = uiState.caloriesBurned
                )
            }

            // ─── Macros ───────────────────────────────────────────────────────
            item {
                MacrosCard(
                    protein = uiState.todayLog?.totalProteinG ?: 0f,
                    carbs = uiState.todayLog?.totalCarbsG ?: 0f,
                    fat = uiState.todayLog?.totalFatG ?: 0f,
                    proteinTarget = uiState.proteinTarget,
                    carbsTarget = uiState.carbsTarget,
                    fatTarget = uiState.fatTarget
                )
            }

            // ─── Water ────────────────────────────────────────────────────────
            item {
                WaterCard(
                    currentMl = uiState.todayLog?.waterMl ?: 0,
                    targetMl = uiState.waterTarget,
                    onAdd = { viewModel.logWater(250) }
                )
            }

            // ─── Meals ────────────────────────────────────────────────────────
            items(MealType.values().toList()) { mealType ->
                val meal = uiState.todayLog?.meals?.find { it.type == mealType }
                MealSection(
                    mealType = mealType,
                    meal = meal,
                    onAddFood = { onAddFood(mealType) },
                    onRemoveFood = { mealId, entryId ->
                        viewModel.removeFoodEntry(mealId, entryId)
                    }
                )
            }
        }
    }
}

@Composable
private fun DateSelector(selectedDate: Long, onDateChange: (Long) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onDateChange(selectedDate - 86400000) }) {
            Icon(Icons.Default.ChevronLeft, null)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if (selectedDate.isToday()) "Azi" else selectedDate.toDateString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(selectedDate.toDateString("EEEE, d MMMM"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = {
            if (!selectedDate.isToday()) onDateChange(selectedDate + 86400000)
        }) {
            Icon(Icons.Default.ChevronRight, null, tint = if (selectedDate.isToday()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f) else LocalContentColor.current)
        }
    }
}

@Composable
private fun CalorieSummaryCard(consumed: Int, target: Int, burned: Int) {
    val remaining = (target - consumed + burned).coerceAtLeast(0)
    val progress = (consumed.toFloat() / target).coerceIn(0f, 1f)

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressRing(progress = progress, size = 90.dp, strokeWidth = 9.dp, color = FitGreen) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$consumed", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = FitGreen)
                    Text("kcal", style = MaterialTheme.typography.labelSmall)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                NutritionStatRow(Icons.Default.TrackChanges, "Obiectiv", "$target kcal", FitBlue)
                NutritionStatRow(Icons.Default.LocalFireDepartment, "Arse", "$burned kcal", FitOrange)
                NutritionStatRow(Icons.Default.Balance, "Rămase", "$remaining kcal", FitGreen)
            }
        }
    }
}

@Composable
private fun NutritionStatRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun MacrosCard(protein: Float, carbs: Float, fat: Float, proteinTarget: Int, carbsTarget: Int, fatTarget: Int) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Macronutrienți", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                MacroBar("Proteine", protein.toInt(), proteinTarget, Color(0xFF4CAF50))
                MacroBar("Carbohidrați", carbs.toInt(), carbsTarget, FitBlue)
                MacroBar("Grăsimi", fat.toInt(), fatTarget, FitOrange)
            }
        }
    }
}

@Composable
private fun MacroBar(name: String, current: Int, target: Int, color: Color) {
    val progress = (current.toFloat() / target).coerceIn(0f, 1f)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("${current}g", fontWeight = FontWeight.Bold, color = color, fontSize = 16.sp)
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(8.dp)
                .height(80.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .fillMaxHeight(progress)
                    .align(Alignment.BottomCenter)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text("/${target}g", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(name, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun WaterCard(currentMl: Int, targetMl: Int, onAdd: () -> Unit) {
    val progress = (currentMl.toFloat() / targetMl).coerceIn(0f, 1f)
    val glasses = currentMl / 250
    val targetGlasses = targetMl / 250

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = FitBlue.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("💧", fontSize = 24.sp)
                    Column {
                        Text("Hidratare", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("${currentMl}ml / ${targetMl}ml", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                IconButton(
                    onClick = onAdd,
                    modifier = Modifier.clip(CircleShape).background(FitBlue).size(40.dp)
                ) {
                    Icon(Icons.Default.Add, null, tint = Color.White)
                }
            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = FitBlue,
                trackColor = FitBlue.copy(alpha = 0.2f)
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(targetGlasses.coerceAtMost(10)) { i ->
                    Text(if (i < glasses) "🥤" else "🫙", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun MealSection(mealType: MealType, meal: Meal?, onAddFood: () -> Unit, onRemoveFood: (String, String) -> Unit) {
    val mealName = when (mealType) {
        MealType.BREAKFAST -> "Mic dejun"
        MealType.LUNCH -> "Prânz"
        MealType.DINNER -> "Cină"
        MealType.SNACK -> "Gustare"
        MealType.PRE_WORKOUT -> "Pre-antrenament"
        MealType.POST_WORKOUT -> "Post-antrenament"
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(mealName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (meal != null) {
                        Text("${meal.totalCalories} kcal", style = MaterialTheme.typography.bodySmall, color = FitGreen, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onAddFood, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Add, null, tint = FitGreen, modifier = Modifier.size(20.dp))
                    }
                }
            }
            if (meal != null && meal.foods.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                meal.foods.forEach { food ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(food.foodName, style = MaterialTheme.typography.bodyMedium)
                            Text("${food.quantity.toInt()}g • ${food.proteinG.toInt()}P • ${food.carbsG.toInt()}C • ${food.fatG.toInt()}G",
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${food.calories} kcal", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { onRemoveFood(meal.id, food.id) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            } else {
                Text("Nicio masă adăugată", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

private fun Long.isToday(): Boolean {
    val cal1 = java.util.Calendar.getInstance().apply { timeInMillis = this@isToday }
    val cal2 = java.util.Calendar.getInstance()
    return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
            cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
}
