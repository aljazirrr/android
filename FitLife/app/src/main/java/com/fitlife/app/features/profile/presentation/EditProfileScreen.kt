package com.fitlife.app.features.profile.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitlife.app.core.domain.model.User
import com.fitlife.app.core.ui.theme.FitGreen
import com.fitlife.app.core.utils.FitnessLevel
import com.fitlife.app.core.utils.Gender

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val user = uiState.user ?: return

    var displayName by remember(user) { mutableStateOf(user.displayName) }
    var age by remember(user) { mutableStateOf(user.age.toString()) }
    var heightCm by remember(user) { mutableStateOf(user.heightCm.toInt().toString()) }
    var weightKg by remember(user) { mutableStateOf(user.weightKg.toString()) }
    var selectedGender by remember(user) { mutableStateOf(user.gender) }
    var selectedFitnessLevel by remember(user) { mutableStateOf(user.fitnessLevel) }
    var weeklyTarget by remember(user) { mutableStateOf(user.weeklyWorkoutTarget.toString()) }
    var calorieTarget by remember(user) { mutableStateOf(user.dailyCalorieTarget.toString()) }
    var waterTarget by remember(user) { mutableStateOf(user.dailyWaterTargetMl.toString()) }
    var stepsTarget by remember(user) { mutableStateOf(user.dailyStepsTarget.toString()) }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            onNavigateBack()
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editează profil", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Înapoi")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.updateProfile(
                                user.copy(
                                    displayName = displayName.ifBlank { user.displayName },
                                    age = age.toIntOrNull() ?: user.age,
                                    heightCm = heightCm.toFloatOrNull() ?: user.heightCm,
                                    weightKg = weightKg.toFloatOrNull() ?: user.weightKg,
                                    gender = selectedGender,
                                    fitnessLevel = selectedFitnessLevel,
                                    weeklyWorkoutTarget = weeklyTarget.toIntOrNull() ?: user.weeklyWorkoutTarget,
                                    dailyCalorieTarget = calorieTarget.toIntOrNull() ?: user.dailyCalorieTarget,
                                    dailyWaterTargetMl = waterTarget.toIntOrNull() ?: user.dailyWaterTargetMl,
                                    dailyStepsTarget = stepsTarget.toIntOrNull() ?: user.dailyStepsTarget
                                )
                            )
                        }
                    ) {
                        Text("Salvează", color = FitGreen, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // ─── Personal Info ────────────────────────────────────────────────
            SectionHeader("Informații personale")

            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Nume") },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FitGreen)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = { Text("Vârstă") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FitGreen)
                )
                OutlinedTextField(
                    value = heightCm,
                    onValueChange = { heightCm = it },
                    label = { Text("Înălțime (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FitGreen)
                )
            }

            OutlinedTextField(
                value = weightKg,
                onValueChange = { weightKg = it },
                label = { Text("Greutate (kg)") },
                leadingIcon = { Icon(Icons.Default.MonitorWeight, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FitGreen)
            )

            // ─── Gender ───────────────────────────────────────────────────────
            SectionHeader("Gen")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Gender.values().forEach { gender ->
                    FilterChip(
                        selected = selectedGender == gender,
                        onClick = { selectedGender = gender },
                        label = { Text(gender.displayName()) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FitGreen,
                            selectedLabelColor = androidx.compose.ui.graphics.Color.White
                        )
                    )
                }
            }

            // ─── Fitness Level ────────────────────────────────────────────────
            SectionHeader("Nivel fitness")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FitnessLevel.values().forEach { level ->
                    FilterChip(
                        selected = selectedFitnessLevel == level,
                        onClick = { selectedFitnessLevel = level },
                        label = { Text(level.displayName()) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FitGreen,
                            selectedLabelColor = androidx.compose.ui.graphics.Color.White
                        )
                    )
                }
            }

            // ─── Daily Targets ────────────────────────────────────────────────
            SectionHeader("Obiective zilnice")

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = weeklyTarget,
                    onValueChange = { weeklyTarget = it },
                    label = { Text("Antren./săpt.") },
                    leadingIcon = { Icon(Icons.Default.FitnessCenter, null, modifier = Modifier.size(18.dp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FitGreen)
                )
                OutlinedTextField(
                    value = calorieTarget,
                    onValueChange = { calorieTarget = it },
                    label = { Text("Calorii/zi") },
                    leadingIcon = { Icon(Icons.Default.LocalFireDepartment, null, modifier = Modifier.size(18.dp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FitGreen)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = waterTarget,
                    onValueChange = { waterTarget = it },
                    label = { Text("Apă (ml/zi)") },
                    leadingIcon = { Icon(Icons.Default.Water, null, modifier = Modifier.size(18.dp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FitGreen)
                )
                OutlinedTextField(
                    value = stepsTarget,
                    onValueChange = { stepsTarget = it },
                    label = { Text("Pași/zi") },
                    leadingIcon = { Icon(Icons.Default.DirectionsWalk, null, modifier = Modifier.size(18.dp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FitGreen)
                )
            }

            if (uiState.error != null) {
                Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
            }

            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = FitGreen)
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = FitGreen
    )
}

fun Gender.displayName() = when (this) {
    Gender.MALE -> "Masculin"
    Gender.FEMALE -> "Feminin"
    Gender.NOT_SPECIFIED -> "Nespecificat"
}
