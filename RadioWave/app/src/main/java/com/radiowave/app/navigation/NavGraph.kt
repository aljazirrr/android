package com.radiowave.app.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.radiowave.app.core.domain.model.RadioStation
import com.radiowave.app.features.archive.presentation.ArchiveScreen
import com.radiowave.app.features.auth.presentation.AuthViewModel
import com.radiowave.app.features.auth.presentation.login.LoginScreen
import com.radiowave.app.features.auth.presentation.register.RegisterScreen
import com.radiowave.app.features.favorites.presentation.FavoritesScreen
import com.radiowave.app.features.home.presentation.HomeScreen
import com.radiowave.app.features.player.presentation.PlayerScreen
import com.radiowave.app.features.player.presentation.PlayerViewModel
import com.radiowave.app.features.profile.presentation.ProfileScreen
import com.radiowave.app.features.recommendations.presentation.RecommendationsScreen
import com.radiowave.app.features.schedule.presentation.ScheduleScreen
import com.radiowave.app.features.search.presentation.SearchScreen
import com.radiowave.app.core.ui.components.RadioStationCard
import com.radiowave.app.core.ui.components.SoundWaveAnimation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Search : Screen("search")
    object Favorites : Screen("favorites")
    object Recommendations : Screen("recommendations")
    object Profile : Screen("profile")
    object Player : Screen("player")
    object Archive : Screen("archive")
    object Schedule : Screen("schedule")
}

data class BottomNavItem(
    val screen: Screen,
    val icon: ImageVector,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, Icons.Default.Home, "Home"),
    BottomNavItem(Screen.Search, Icons.Default.Search, "Search"),
    BottomNavItem(Screen.Favorites, Icons.Default.Favorite, "Favorites"),
    BottomNavItem(Screen.Recommendations, Icons.Default.AutoAwesome, "For You"),
    BottomNavItem(Screen.Profile, Icons.Default.Person, "Profile")
)

@Composable
fun RadioWaveNavGraph(
    navController: NavHostController,
    isDarkTheme: Boolean
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val currentUser by authViewModel.currentUser.collectAsState()

    val startDestination = if (currentUser != null) Screen.Home.route else Screen.Login.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onForgotPassword = {}
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            val playerViewModel: PlayerViewModel = hiltViewModel()
            val playerState by playerViewModel.playerUiState.collectAsState()
            MainScaffold(
                navController = navController,
                playerViewModel = playerViewModel
            ) {
                HomeScreen(
                    onStationClick = { station ->
                        playerViewModel.playStation(station)
                        navController.navigate(Screen.Player.route)
                    },
                    onSearchClick = { navController.navigate(Screen.Search.route) },
                    onTagClick = { tag ->
                        navController.navigate("${Screen.Search.route}?tag=$tag")
                    },
                    onCountryClick = { country ->
                        navController.navigate("${Screen.Search.route}?country=$country")
                    },
                    currentPlayingId = playerState.currentStation?.stationUuid
                )
            }
        }

        composable(Screen.Search.route) {
            val playerViewModel: PlayerViewModel = hiltViewModel()
            val playerState by playerViewModel.playerUiState.collectAsState()
            MainScaffold(
                navController = navController,
                playerViewModel = playerViewModel
            ) {
                SearchScreen(
                    onStationClick = { station ->
                        playerViewModel.playStation(station)
                        navController.navigate(Screen.Player.route)
                    },
                    onBack = { navController.popBackStack() },
                    currentPlayingId = playerState.currentStation?.stationUuid
                )
            }
        }

        composable(Screen.Favorites.route) {
            val playerViewModel: PlayerViewModel = hiltViewModel()
            val playerState by playerViewModel.playerUiState.collectAsState()
            MainScaffold(
                navController = navController,
                playerViewModel = playerViewModel
            ) {
                FavoritesScreen(
                    onStationClick = { station ->
                        playerViewModel.playStation(station)
                        navController.navigate(Screen.Player.route)
                    },
                    currentPlayingId = playerState.currentStation?.stationUuid
                )
            }
        }

        composable(Screen.Recommendations.route) {
            val playerViewModel: PlayerViewModel = hiltViewModel()
            val playerState by playerViewModel.playerUiState.collectAsState()
            MainScaffold(
                navController = navController,
                playerViewModel = playerViewModel
            ) {
                RecommendationsScreen(
                    onStationClick = { station ->
                        playerViewModel.playStation(station)
                        navController.navigate(Screen.Player.route)
                    },
                    currentPlayingId = playerState.currentStation?.stationUuid
                )
            }
        }

        composable(Screen.Profile.route) {
            val playerViewModel: PlayerViewModel = hiltViewModel()
            MainScaffold(
                navController = navController,
                playerViewModel = playerViewModel
            ) {
                ProfileScreen(
                    onNavigateToArchive = { navController.navigate(Screen.Archive.route) },
                    onNavigateToSchedule = { navController.navigate(Screen.Schedule.route) }
                )
            }
        }

        composable(Screen.Player.route) {
            val playerViewModel: PlayerViewModel = hiltViewModel()
            var showRecording by remember { mutableStateOf(false) }
            PlayerScreen(
                onDismiss = { navController.popBackStack() },
                onRecordClick = { showRecording = true },
                onShareClick = {}
            )
        }

        composable(Screen.Archive.route) {
            ArchiveScreen()
        }

        composable(Screen.Schedule.route) {
            ScheduleScreen()
        }
    }
}

@Composable
private fun MainScaffold(
    navController: NavHostController,
    playerViewModel: PlayerViewModel,
    content: @Composable () -> Unit
) {
    val playerState by playerViewModel.playerUiState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            Column {
                // Mini player
                AnimatedVisibility(
                    visible = playerState.currentStation != null,
                    enter = slideInVertically { it },
                    exit = slideOutVertically { it }
                ) {
                    playerState.currentStation?.let { station ->
                        MiniPlayer(
                            station = station,
                            isPlaying = playerState.playerState == com.radiowave.app.core.domain.model.PlayerState.PLAYING,
                            onPlayPause = { playerViewModel.togglePlayPause() },
                            onClick = { navController.navigate(Screen.Player.route) }
                        )
                    }
                }
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, item.label) },
                            label = { Text(item.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            content()
        }
    }
}

@Composable
private fun MiniPlayer(
    station: RadioStation,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxSize(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isPlaying) {
                SoundWaveAnimation(
                    isPlaying = true,
                    barCount = 3,
                    modifier = Modifier.size(24.dp)
                )
                androidx.compose.foundation.layout.Spacer(Modifier.padding(4.dp))
            }
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    station.name,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1
                )
                if (station.country.isNotEmpty()) {
                    Text(
                        station.country,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            IconButton(onClick = onPlayPause) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    if (isPlaying) "Pause" else "Play",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
