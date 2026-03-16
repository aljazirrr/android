package com.fitlife.app.features.auth.presentation.register

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitlife.app.core.ui.components.*
import com.fitlife.app.core.ui.theme.FitGreen
import com.fitlife.app.features.auth.presentation.AuthViewModel

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val registerForm by viewModel.registerForm.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) onRegisterSuccess()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(60.dp))

            // Header
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(FitGreen, Color(0xFF00BCD4)))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PersonAdd, null,
                    modifier = Modifier.size(48.dp),
                    tint = Color.White
                )
            }

            Spacer(Modifier.height(20.dp))
            Text("Creează cont", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
            Text(
                "Începe-ți călătoria fitness astăzi",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            // Register Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FitTextField(
                        value = registerForm.displayName,
                        onValueChange = viewModel::onRegisterNameChange,
                        label = "Numele tău",
                        leadingIcon = Icons.Default.Person,
                        error = registerForm.nameError,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )

                    FitTextField(
                        value = registerForm.email,
                        onValueChange = viewModel::onRegisterEmailChange,
                        label = "Email",
                        leadingIcon = Icons.Default.Email,
                        error = registerForm.emailError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        )
                    )

                    FitTextField(
                        value = registerForm.password,
                        onValueChange = viewModel::onRegisterPasswordChange,
                        label = "Parolă",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        error = registerForm.passwordError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        )
                    )

                    FitTextField(
                        value = registerForm.confirmPassword,
                        onValueChange = viewModel::onRegisterConfirmPasswordChange,
                        label = "Confirmă parola",
                        leadingIcon = Icons.Default.LockReset,
                        isPassword = true,
                        error = registerForm.confirmPasswordError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        )
                    )

                    // Password requirements
                    if (registerForm.password.isNotEmpty()) {
                        PasswordStrengthIndicator(password = registerForm.password)
                    }

                    AnimatedVisibility(visible = uiState.error != null) {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(uiState.error ?: "", color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    GradientButton(
                        text = "Înregistrare",
                        onClick = viewModel::register,
                        enabled = registerForm.isFormValid,
                        isLoading = uiState.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        "Prin înregistrare ești de acord cu Termenii și Condițiile noastre.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Ai deja cont? ", color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = onNavigateToLogin) {
                    Text("Autentifică-te", color = FitGreen, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(32.dp))
        }

        LoadingOverlay(isVisible = uiState.isLoading)
    }
}

@Composable
private fun PasswordStrengthIndicator(password: String) {
    val requirements = listOf(
        "Minim 8 caractere" to (password.length >= 8),
        "Literă mare" to password.any { it.isUpperCase() },
        "Cifră" to password.any { it.isDigit() },
        "Caracter special" to password.any { !it.isLetterOrDigit() }
    )
    val strength = requirements.count { it.second }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            if (index < strength) when {
                                strength <= 1 -> Color.Red
                                strength <= 2 -> Color(0xFFFF9800)
                                strength <= 3 -> Color(0xFFFFEB3B)
                                else -> FitGreen
                            } else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }
        requirements.forEach { (text, met) ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(
                    if (met) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    null,
                    modifier = Modifier.size(14.dp),
                    tint = if (met) FitGreen else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(text, style = MaterialTheme.typography.labelSmall, color = if (met) FitGreen else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
