package com.bariatric.assistant.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bariatric.assistant.features.chat.ChatScreen
import com.bariatric.assistant.features.hydration.HydrationScreen
import com.bariatric.assistant.features.journal.FoodJournalScreen
import com.bariatric.assistant.features.onboarding.OnboardingScreen
import com.bariatric.assistant.features.tips.DailyTipsScreen

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Chat : Screen("chat")
    data object Hydration : Screen("hydration")
    data object FoodJournal : Screen("food_journal")
    data object DailyTips : Screen("daily_tips")
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Chat, "Chat", Icons.Filled.Chat, Icons.Outlined.Chat),
    BottomNavItem(Screen.Hydration, "Hidratare", Icons.Filled.LocalDrink, Icons.Outlined.LocalDrink),
    BottomNavItem(Screen.FoodJournal, "Jurnal", Icons.Filled.MenuBook, Icons.Outlined.MenuBook),
    BottomNavItem(Screen.DailyTips, "Sfaturi", Icons.Filled.TipsAndUpdates, Icons.Outlined.TipsAndUpdates)
)

@Composable
fun BariatricNavGraph(
    startOnboarding: Boolean
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.route != Screen.Onboarding.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = androidx.compose.ui.unit.dp.times(2)
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) },
                            selected = selected,
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
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (startOnboarding) Screen.Onboarding.route else Screen.Chat.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300))
            }
        ) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onOnboardingComplete = {
                        navController.navigate(Screen.Chat.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Chat.route) {
                ChatScreen()
            }
            composable(Screen.Hydration.route) {
                HydrationScreen()
            }
            composable(Screen.FoodJournal.route) {
                FoodJournalScreen()
            }
            composable(Screen.DailyTips.route) {
                DailyTipsScreen()
            }
        }
    }
}
