package com.fooddiary.app

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.fooddiary.app.domain.model.MealType
import com.fooddiary.app.ui.screens.addfood.AddFoodScreen
import com.fooddiary.app.ui.screens.diary.DiaryScreen
import com.fooddiary.app.ui.screens.diary.startOfDay
import com.fooddiary.app.ui.screens.summary.WeeklySummaryScreen

object Routes {
    const val DIARY = "diary"
    const val ADD_FOOD = "add_food/{date}/{mealType}"
    const val WEEKLY_SUMMARY = "weekly_summary"

    fun addFood(date: Long, mealType: MealType) = "add_food/$date/${mealType.name}"
}

@Composable
fun FoodDiaryNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.DIARY,
        enterTransition = { slideInHorizontally(tween(300)) { it } + fadeIn(tween(300)) },
        exitTransition = { slideOutHorizontally(tween(300)) { -it } + fadeOut(tween(300)) },
        popEnterTransition = { slideInHorizontally(tween(300)) { -it } + fadeIn(tween(300)) },
        popExitTransition = { slideOutHorizontally(tween(300)) { it } + fadeOut(tween(300)) }
    ) {
        composable(Routes.DIARY) {
            DiaryScreen(
                onAddFood = { date, mealType ->
                    navController.navigate(Routes.addFood(date, mealType))
                },
                onNavigateToSummary = {
                    navController.navigate(Routes.WEEKLY_SUMMARY)
                }
            )
        }

        composable(Routes.ADD_FOOD) { backStackEntry ->
            val date = backStackEntry.arguments?.getString("date")?.toLongOrNull()
                ?: startOfDay(System.currentTimeMillis())
            val mealTypeName = backStackEntry.arguments?.getString("mealType") ?: "BREAKFAST"
            val mealType = try { MealType.valueOf(mealTypeName) } catch (_: Exception) { MealType.BREAKFAST }

            AddFoodScreen(
                date = date,
                mealType = mealType,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.WEEKLY_SUMMARY) {
            WeeklySummaryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
