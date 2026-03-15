package com.fitlife.app.features.profile.presentation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitlife.app.core.domain.model.User
import com.fitlife.app.core.ui.theme.*
import com.fitlife.app.core.utils.FitnessLevel
import com.fitlife.app.core.utils.Gender
import com.fitlife.app.core.utils.toBmi
import com.fitlife.app.core.utils.toBmiCategory
import com.fitlife.app.core.utils.toDateString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onEditProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSignOutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
    ) {
        // ─── Profile Header ───────────────────────────────────────────────────
        uiState.user?.let { user ->
            ProfileHeader(user = user, onEditProfile = onEditProfile)
            Spacer(Modifier.height(16.dp))

            // ─── Stats Summary ────────────────────────────────────────────────
            ProfileStatsRow(user = user)

            Spacer(Modifier.height(16.dp))

            // ─── Goals ────────────────────────────────────────────────────────
            ProfileGoalsCard(user = user, onEdit = onEditProfile)

            Spacer(Modifier.height(16.dp))
        }

        // ─── Menu ──────────────────────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column {
                ProfileMenuItem(Icons.Default.Edit, "Editează profil", onClick = onEditProfile)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                ProfileMenuItem(Icons.Default.Settings, "Setări", onClick = onNavigateToSettings)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                ProfileMenuItem(Icons.Default.Notifications, "Notificări", onClick = {})
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                ProfileMenuItem(Icons.Default.Security, "Securitate", onClick = {})
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                ProfileMenuItem(Icons.Default.Help, "Ajutor & Suport", onClick = {})
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                ProfileMenuItem(Icons.Default.Info, "Despre FitLife", onClick = {})
            }
        }

        Spacer(Modifier.height(16.dp))

        // ─── Sign Out ──────────────────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            ProfileMenuItem(
                icon = Icons.Default.Logout,
                title = "Deconectare",
                tint = MaterialTheme.colorScheme.error,
                onClick = { showSignOutDialog = true }
            )
        }

        Spacer(Modifier.height(32.dp))

        // Version info
        Text(
            "FitLife v1.0.0",
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }

    // ─── Sign Out Dialog ──────────────────────────────────────────────────────
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            icon = { Icon(Icons.Default.Logout, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Deconectare") },
            text = { Text("Ești sigur că vrei să te deconectezi?") },
            confirmButton = {
                Button(
                    onClick = { showSignOutDialog = false; viewModel.signOut(); onSignOut() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Deconectare") }
            },
            dismissButton = { TextButton(onClick = { showSignOutDialog = false }) { Text("Anulează") } }
        )
    }
}

@Composable
private fun ProfileHeader(user: User, onEditProfile: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(FitGreen.copy(alpha = 0.2f), Color.Transparent)))
            .padding(24.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            // Avatar
            Box(
                modifier = Modifier.size(100.dp).clip(CircleShape).background(
                    Brush.linearGradient(listOf(FitGreen, Color(0xFF00BCD4)))
                ),
                contentAlignment = Alignment.Center
            ) {
                val initial = user.displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "U"
                Text(initial, fontSize = 40.sp, color = Color.White, fontWeight = FontWeight.ExtraBold)
            }

            Spacer(Modifier.height(12.dp))
            Text(user.displayName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            Text(user.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            AssistChip(
                onClick = onEditProfile,
                label = { Text(user.fitnessLevel.displayName(), style = MaterialTheme.typography.labelMedium) },
                leadingIcon = { Text(user.fitnessLevel.emoji(), fontSize = 14.sp) },
                colors = AssistChipDefaults.assistChipColors(containerColor = FitGreen.copy(alpha = 0.15f))
            )
        }
    }
}

@Composable
private fun ProfileStatsRow(user: User) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatChip("${user.heightCm.toInt()}cm", "Înălțime", FitBlue, modifier = Modifier.weight(1f))
        StatChip("${user.weightKg}kg", "Greutate", FitGreen, modifier = Modifier.weight(1f))
        if (user.heightCm > 0 && user.weightKg > 0) {
            val bmi = user.weightKg.toBmi(user.heightCm)
            StatChip("$bmi", "IMC", FitOrange, modifier = Modifier.weight(1f))
        }
        StatChip("${user.age}", "Vârstă", FitPurple, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatChip(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontWeight = FontWeight.ExtraBold, color = color, fontSize = 16.sp)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ProfileGoalsCard(user: User, onEdit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Obiective zilnice", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                TextButton(onClick = onEdit) { Text("Editează", color = FitGreen) }
            }
            Spacer(Modifier.height(12.dp))
            GoalItem(Icons.Default.FitnessCenter, "Antrenamente/săptămână", "${user.weeklyWorkoutTarget}")
            GoalItem(Icons.Default.Restaurant, "Calorii/zi", "${user.dailyCalorieTarget} kcal")
            GoalItem(Icons.Default.DirectionsWalk, "Pași/zi", "${user.dailyStepsTarget}")
            GoalItem(Icons.Default.Water, "Apă/zi", "${user.dailyWaterTargetMl} ml")
        }
    }
}

@Composable
private fun GoalItem(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, null, tint = FitGreen, modifier = Modifier.size(18.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = FitGreen)
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge, color = tint, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
    }
}

fun FitnessLevel.displayName() = when (this) {
    FitnessLevel.BEGINNER -> "Începător"
    FitnessLevel.INTERMEDIATE -> "Intermediar"
    FitnessLevel.ADVANCED -> "Avansat"
    FitnessLevel.ATHLETE -> "Atlet"
}

fun FitnessLevel.emoji() = when (this) {
    FitnessLevel.BEGINNER -> "🌱"
    FitnessLevel.INTERMEDIATE -> "💪"
    FitnessLevel.ADVANCED -> "🔥"
    FitnessLevel.ATHLETE -> "🏆"
}
