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
import com.fitlife.app.features.nutrition.presentation.AddFoodScreen
import com.fitlife.app.features.nutrition.presentation.NutritionScreen
import com.fitlife.app.core.utils.MealType
import com.fitlife.app.features.profile.presentation.ProfileScreen
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
    isAuthenticated: Boolean,
    startDestination: String = if (isAuthenticated) Screen.Dashboard.route else Screen.Login.route
) {
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

        composable(Screen.Progress.route) {
            ProgressScreen(onAddMeasurement = { navController.navigate(Screen.AddMeasurement.route) })
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onEditProfile = { },
                onNavigateToSettings = { },
                onSignOut = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
