package com.fitlife.app.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.navigation.*
import androidx.navigation.compose.*
import com.fitlife.app.features.auth.presentation.forgot.ForgotPasswordScreen
import com.fitlife.app.features.auth.presentation.login.LoginScreen
import com.fitlife.app.features.auth.presentation.register.RegisterScreen
import com.fitlife.app.features.dashboard.presentation.DashboardScreen
import com.fitlife.app.features.exercises.presentation.ExerciseListScreen
import com.fitlife.app.features.exercises.presentation.ExerciseDetailScreen
import com.fitlife.app.features.nutrition.presentation.AddFoodScreen
import com.fitlife.app.features.nutrition.presentation.NutritionScreen
import com.fitlife.app.core.utils.MealType
import com.fitlife.app.features.profile.presentation.ProfileScreen
import com.fitlife.app.features.profile.presentation.EditProfileScreen
import com.fitlife.app.features.profile.presentation.SettingsScreen
import com.fitlife.app.features.profile.presentation.NotificationsScreen
import com.fitlife.app.features.profile.presentation.SecurityScreen
import com.fitlife.app.features.profile.presentation.HelpScreen
import com.fitlife.app.features.profile.presentation.AboutScreen
import com.fitlife.app.features.progress.presentation.AddMeasurementScreen
import com.fitlife.app.features.progress.presentation.ProgressScreen
import com.fitlife.app.features.workout.presentation.active.ActiveWorkoutScreen
import com.fitlife.app.features.workout.presentation.list.WorkoutListScreen

sealed class Screen(val route: String) {
    // Auth
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")

    // Main
    object Dashboard : Screen("dashboard")
    object Workout : Screen("workout")
    object ActiveWorkout : Screen("active_workout")
    object Exercises : Screen("exercises")
    object Nutrition : Screen("nutrition")
    object Progress : Screen("progress")
    object Profile : Screen("profile")
    object AddMeasurement : Screen("add_measurement")
    object EditProfile : Screen("edit_profile")
    object Settings : Screen("settings")
    object Notifications : Screen("notifications")
    object Security : Screen("security")
    object Help : Screen("help")
    object About : Screen("about")
    object AddFood : Screen("add_food/{mealType}") {
        fun createRoute(mealType: MealType) = "add_food/${mealType.name}"
    }
    object ExerciseDetail : Screen("exercise_detail/{exerciseId}") {
        fun createRoute(exerciseId: String) = "exercise_detail/$exerciseId"
    }
}

@Composable
fun FitLifeNavGraph(
    navController: NavHostController,
    isAuthenticated: Boolean
) {
    // remember ensures startDestination is computed ONCE — prevents NavHost from
    // recreating the entire graph when isAuthenticated changes (which would cause
    // "destination not found" crashes on in-flight navigations).
    val startDestination = remember {
        if (isAuthenticated) Screen.Dashboard.route else Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { slideInHorizontally(tween(300)) { it } + fadeIn(tween(300)) },
        exitTransition = { slideOutHorizontally(tween(300)) { -it } + fadeOut(tween(300)) },
        popEnterTransition = { slideInHorizontally(tween(300)) { -it } + fadeIn(tween(300)) },
        popExitTransition = { slideOutHorizontally(tween(300)) { it } + fadeOut(tween(300)) }
    ) {
        // ─── Auth ──────────────────────────────────────────────────────────────
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(onNavigateBack = { navController.popBackStack() })
        }

        // ─── Main ──────────────────────────────────────────────────────────────
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToWorkout = { navController.navigate(Screen.Workout.route) },
                onNavigateToNutrition = { navController.navigate(Screen.Nutrition.route) },
                onNavigateToProgress = { navController.navigate(Screen.Progress.route) },
                onNavigateToExercises = { navController.navigate(Screen.Exercises.route) },
                onStartWorkout = { navController.navigate(Screen.ActiveWorkout.route) }
            )
        }

        composable(Screen.Workout.route) {
            WorkoutListScreen(
                onStartWorkout = { navController.navigate(Screen.ActiveWorkout.route) },
                onViewSession = { /* detail screen not yet implemented */ }
            )
        }

        composable(Screen.ActiveWorkout.route) {
            ActiveWorkoutScreen(
                onWorkoutFinished = { navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Exercises.route) {
            ExerciseListScreen(
                onExerciseClick = { id -> navController.navigate(Screen.ExerciseDetail.createRoute(id)) },
                onAddCustomExercise = { /* Show dialog */ }
            )
        }

        composable(Screen.Nutrition.route) {
            NutritionScreen(
                onAddFood = { mealType ->
                    navController.navigate(Screen.AddFood.createRoute(mealType))
                }
            )
        }

        composable(Screen.AddFood.route) { backStackEntry ->
            val mealTypeName = backStackEntry.arguments?.getString("mealType") ?: "BREAKFAST"
            val mealType = try { MealType.valueOf(mealTypeName) } catch (e: Exception) { MealType.BREAKFAST }
            AddFoodScreen(
                mealType = mealType,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ExerciseDetail.route) { backStackEntry ->
            val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: return@composable
            ExerciseDetailScreen(
                exerciseId = exerciseId,
                onNavigateBack = { navController.popBackStack() },
                onStartWorkout = { navController.navigate(Screen.ActiveWorkout.route) }
            )
        }

        composable(Screen.Progress.route) {
            ProgressScreen(onAddMeasurement = { navController.navigate(Screen.AddMeasurement.route) })
        }

        composable(Screen.AddMeasurement.route) {
            AddMeasurementScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onEditProfile = { navController.navigate(Screen.EditProfile.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                onNavigateToSecurity = { navController.navigate(Screen.Security.route) },
                onNavigateToHelp = { navController.navigate(Screen.Help.route) },
                onNavigateToAbout = { navController.navigate(Screen.About.route) },
                onSignOut = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.EditProfile.route) {
            EditProfileScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Settings.route) {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Security.route) {
            SecurityScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Help.route) {
            HelpScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.About.route) {
            AboutScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
