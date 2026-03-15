package com.fitlife.app.features.progress.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fitlife.app.core.domain.model.BodyMeasurement
import com.fitlife.app.core.ui.components.GradientButton
import com.fitlife.app.core.ui.theme.FitGreen
import com.fitlife.app.features.auth.presentation.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMeasurementScreen(
    onNavigateBack: () -> Unit,
    progressViewModel: ProgressViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.uiState.collectAsState()

    var weightKg by remember { mutableStateOf("") }
    var bodyFatPercent by remember { mutableStateOf("") }
    var chestCm by remember { mutableStateOf("") }
    var waistCm by remember { mutableStateOf("") }
    var hipsCm by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Adaugă măsurătoare", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Înapoi")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Greutate și compoziție", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = FitGreen)

            OutlinedTextField(
                value = weightKg,
                onValueChange = { weightKg = it },
                label = { Text("Greutate (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = bodyFatPercent,
                onValueChange = { bodyFatPercent = it },
                label = { Text("Grăsime corporală (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            HorizontalDivider()

            Text("Circumferințe (cm)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = FitGreen)

            OutlinedTextField(
                value = chestCm,
                onValueChange = { chestCm = it },
                label = { Text("Piept (cm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = waistCm,
                onValueChange = { waistCm = it },
                label = { Text("Talie (cm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = hipsCm,
                onValueChange = { hipsCm = it },
                label = { Text("Șolduri (cm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            HorizontalDivider()

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notițe (opțional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            Spacer(Modifier.height(8.dp))

            GradientButton(
                text = "Salvează",
                onClick = {
                    val userId = authState.currentUser?.uid ?: return@GradientButton
                    val weight = weightKg.toFloatOrNull()
                    val fat = bodyFatPercent.toFloatOrNull()
                    val chest = chestCm.toFloatOrNull()
                    val waist = waistCm.toFloatOrNull()
                    val hips = hipsCm.toFloatOrNull()

                    val bmi = if (weight != null) {
                        val userHeight = authState.currentUser?.heightCm ?: 170f
                        if (userHeight > 0f) {
                            val hM = userHeight / 100f
                            kotlin.math.round(weight / (hM * hM) * 10f) / 10f
                        } else null
                    } else null

                    progressViewModel.addMeasurement(
                        BodyMeasurement(
                            userId = userId,
                            weightKg = weight,
                            bodyFatPercent = fat,
                            bmi = bmi,
                            chestCm = chest,
                            waistCm = waist,
                            hipsCm = hips,
                            notes = notes
                        )
                    )
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
