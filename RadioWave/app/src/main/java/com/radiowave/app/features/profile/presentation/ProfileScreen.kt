package com.radiowave.app.features.profile.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.radiowave.app.core.ui.theme.*

@Composable
fun ProfileScreen(
    onNavigateToArchive: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    var showSignOutDialog by remember { mutableStateOf(false) }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutDialog = false
                        viewModel.signOut()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Sign Out") }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // User info
            item {
                Card(
                    modifier = Modifier.padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (userProfile?.photoUrl != null) {
                                AsyncImage(
                                    model = userProfile!!.photoUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            userProfile?.displayName?.firstOrNull()?.uppercase() ?: "?",
                                            style = MaterialTheme.typography.headlineMedium,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                userProfile?.displayName ?: "User",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                userProfile?.email ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            // Settings section
            item {
                SettingsSectionHeader("Appearance")
            }

            item {
                SettingsRow(
                    icon = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                    title = "Dark Theme",
                    subtitle = if (isDarkTheme) "Currently enabled" else "Currently disabled",
                    trailing = {
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { viewModel.toggleTheme() }
                        )
                    }
                )
            }

            item { SettingsSectionHeader("My Content") }

            item {
                SettingsRow(
                    icon = Icons.Default.LibraryMusic,
                    title = "My Archive",
                    subtitle = "Manage your recordings",
                    trailing = { Icon(Icons.Default.ChevronRight, null) },
                    onClick = onNavigateToArchive
                )
            }

            item {
                SettingsRow(
                    icon = Icons.Default.Schedule,
                    title = "Scheduled Recordings",
                    subtitle = "Manage scheduled recordings",
                    trailing = { Icon(Icons.Default.ChevronRight, null) },
                    onClick = onNavigateToSchedule
                )
            }

            item { SettingsSectionHeader("Account") }

            item {
                SettingsRow(
                    icon = Icons.Default.Logout,
                    title = "Sign Out",
                    subtitle = "Sign out of your account",
                    trailing = null,
                    onClick = { showSignOutDialog = true },
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    trailing: @Composable (() -> Unit)?,
    onClick: (() -> Unit)? = null,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Card(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        onClick = onClick ?: {}
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                null,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, color = tint)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            trailing?.invoke()
        }
    }
}
