package com.bariatric.assistant.features.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bariatric.assistant.components.BariatricTextField
import com.bariatric.assistant.components.GradientButton
import com.bariatric.assistant.components.SelectableChip
import com.bariatric.assistant.core.data.preferences.PostOpStage
import com.bariatric.assistant.core.data.preferences.SurgeryType

@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (uiState.currentStep > 0) {
                    IconButton(onClick = { viewModel.previousStep() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Înapoi"
                        )
                    }
                }
                LinearProgressIndicator(
                    progress = { (uiState.currentStep + 1) / 3f },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .padding(horizontal = 8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Text(
                    text = "${uiState.currentStep + 1}/3",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedContent(
                targetState = uiState.currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                    } else {
                        slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                    }
                },
                modifier = Modifier.weight(1f),
                label = "onboarding_step"
            ) { step ->
                when (step) {
                    0 -> WelcomeStep(
                        userName = uiState.userName,
                        onNameChange = viewModel::updateUserName
                    )
                    1 -> SurgeryTypeStep(
                        selected = uiState.selectedSurgeryType,
                        onSelect = viewModel::selectSurgeryType
                    )
                    2 -> PostOpStageStep(
                        selected = uiState.selectedPostOpStage,
                        onSelect = viewModel::selectPostOpStage
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            GradientButton(
                text = if (uiState.currentStep == 2) "Începe" else "Continuă",
                onClick = {
                    if (uiState.currentStep == 2) {
                        viewModel.completeOnboarding(onOnboardingComplete)
                    } else {
                        viewModel.nextStep()
                    }
                },
                enabled = viewModel.canProceed(),
                isLoading = uiState.isCompleting
            )
        }
    }
}

@Composable
private fun WelcomeStep(
    userName: String,
    onNameChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.MedicalServices,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Bun venit!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Sunt asistentul tău dietetician specializat\nîn chirurgie bariatrică.\nHai să ne cunoaștem!",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(40.dp))

        BariatricTextField(
            value = userName,
            onValueChange = onNameChange,
            label = "Cum te numești?",
            placeholder = "Numele tău",
            leadingIcon = Icons.Default.Person
        )
    }
}

@Composable
private fun SurgeryTypeStep(
    selected: SurgeryType?,
    onSelect: (SurgeryType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Icon(
            Icons.Default.MedicalServices,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Ce tip de intervenție\nai avut?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Acest lucru ne ajută să personalizăm recomandările nutriționale.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        val surgeryDescriptions = mapOf(
            SurgeryType.SLEEVE to "Reducerea stomacului la forma unui tub",
            SurgeryType.BYPASS to "Redirecționarea tractului digestiv",
            SurgeryType.BAND to "Bandă ajustabilă în jurul stomacului",
            SurgeryType.DUODENAL_SWITCH to "Procedură combinată complexă",
            SurgeryType.MINI_BYPASS to "Versiune simplificată a bypass-ului"
        )

        SurgeryType.entries.forEach { type ->
            SelectableChip(
                text = type.displayName,
                selected = selected == type,
                onClick = { onSelect(type) },
                description = surgeryDescriptions[type]
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PostOpStageStep(
    selected: PostOpStage?,
    onSelect: (PostOpStage) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Icon(
            Icons.Default.Schedule,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "În ce stadiu\nte afli?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Fiecare stadiu post-operator are nevoi nutriționale diferite.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        PostOpStage.entries.forEach { stage ->
            SelectableChip(
                text = stage.displayName,
                selected = selected == stage,
                onClick = { onSelect(stage) },
                description = stage.description
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
