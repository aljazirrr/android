package com.fooddiary.app.ui.screens.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fooddiary.app.data.repository.FoodDiaryRepository
import com.fooddiary.app.domain.model.*
import com.fooddiary.app.ui.screens.diary.startOfDay
import com.fooddiary.app.ui.screens.diary.startOfWeek
import com.fooddiary.app.ui.screens.diary.toDateString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToInt

data class WeeklySummaryUiState(
    val isLoading: Boolean = true,
    val summary: WeeklySummary? = null,
    val weekStart: Long = startOfWeek(System.currentTimeMillis()),
    val error: String? = null
)

@HiltViewModel
class WeeklySummaryViewModel @Inject constructor(
    private val repository: FoodDiaryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeeklySummaryUiState())
    val uiState: StateFlow<WeeklySummaryUiState> = _uiState.asStateFlow()

    // Targets — could be user-configurable
    private val calorieTarget = 2000
    private val proteinTarget = 150
    private val carbsTarget = 250
    private val fatTarget = 65
    private val waterTarget = 2500

    init {
        loadWeek()
    }

    fun previousWeek() {
        val cal = Calendar.getInstance().apply {
            timeInMillis = _uiState.value.weekStart
            add(Calendar.DAY_OF_YEAR, -7)
        }
        _uiState.update { it.copy(weekStart = startOfWeek(cal.timeInMillis)) }
        loadWeek()
    }

    fun nextWeek() {
        val next = Calendar.getInstance().apply {
            timeInMillis = _uiState.value.weekStart
            add(Calendar.DAY_OF_YEAR, 7)
        }.timeInMillis
        if (startOfWeek(next) > startOfWeek(System.currentTimeMillis())) return
        _uiState.update { it.copy(weekStart = startOfWeek(next)) }
        loadWeek()
    }

    private fun loadWeek() {
        val weekStart = _uiState.value.weekStart
        val weekEnd = weekStart + 7 * 86400000L - 1
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            combine(
                repository.getEntriesInRange(weekStart, weekEnd),
                // Collect water/notes for each day individually would be complex,
                // so we work with entries only for the summary charts
                flowOf(Unit)
            ) { entries, _ ->
                buildSummary(entries, weekStart, weekEnd)
            }.collect { summary ->
                _uiState.update { it.copy(isLoading = false, summary = summary) }
            }
        }
    }

    private fun buildSummary(entries: List<FoodEntry>, weekStart: Long, weekEnd: Long): WeeklySummary {
        val dayLabels = listOf("Luni", "Marți", "Miercuri", "Joi", "Vineri", "Sâmbătă", "Duminică")
        val entriesByDay = entries.groupBy { it.dayDate }

        val days = (0..6).map { offset ->
            val dayDate = startOfDay(weekStart + offset * 86400000L)
            val dayEntries = entriesByDay[dayDate] ?: emptyList()
            val mealBreakdown = MealType.entries.associateWith { type ->
                dayEntries.filter { it.mealType == type }
            }
            DaySummary(
                date = dayDate,
                dayLabel = dayLabels[offset % 7],
                entries = dayEntries,
                totalCalories = dayEntries.sumOf { it.calories },
                totalProteinG = dayEntries.map { it.proteinG }.sum(),
                totalCarbsG = dayEntries.map { it.carbsG }.sum(),
                totalFatG = dayEntries.map { it.fatG }.sum(),
                totalFiberG = dayEntries.map { it.fiberG }.sum(),
                totalSugarG = dayEntries.map { it.sugarG }.sum(),
                waterMl = 0,
                mealBreakdown = mealBreakdown
            )
        }

        val loggedDays = days.filter { it.totalCalories > 0 }
        val daysLogged = loggedDays.size

        val avgCal = if (daysLogged > 0) loggedDays.sumOf { it.totalCalories } / daysLogged else 0
        val avgProt = if (daysLogged > 0) loggedDays.map { it.totalProteinG }.average().toFloat() else 0f
        val avgCarbs = if (daysLogged > 0) loggedDays.map { it.totalCarbsG }.average().toFloat() else 0f
        val avgFat = if (daysLogged > 0) loggedDays.map { it.totalFatG }.average().toFloat() else 0f
        val avgFiber = if (daysLogged > 0) loggedDays.map { it.totalFiberG }.average().toFloat() else 0f

        val bestDay = loggedDays.minByOrNull { abs(it.totalCalories - calorieTarget) }
        val worstDay = loggedDays.maxByOrNull { abs(it.totalCalories - calorieTarget) }

        val tips = generateTips(loggedDays, daysLogged)

        return WeeklySummary(
            weekStart = weekStart,
            weekEnd = weekEnd,
            days = days,
            daysLogged = daysLogged,
            avgCalories = avgCal,
            avgProteinG = avgProt,
            avgCarbsG = avgCarbs,
            avgFatG = avgFat,
            avgFiberG = avgFiber,
            totalWaterMl = 0,
            avgWaterMl = 0,
            bestDay = bestDay,
            worstDay = if (worstDay != bestDay) worstDay else null,
            tips = tips
        )
    }

    private fun generateTips(loggedDays: List<DaySummary>, daysLogged: Int): List<NutritionTip> {
        if (daysLogged == 0) return emptyList()
        val tips = mutableListOf<NutritionTip>()

        // WARNING: Low protein
        val lowProtein = loggedDays.count { it.totalProteinG < proteinTarget * 0.8f }
        if (lowProtein >= 2) {
            tips += NutritionTip("🥩", "Ai consumat prea puține proteine în $lowProtein din $daysLogged zile. Încearcă să adaugi mai multă carne, ouă sau lactate.", TipSeverity.WARNING)
        }

        // WARNING: High calories
        val highCal = loggedDays.count { it.totalCalories > calorieTarget * 1.2 }
        if (highCal >= 2) {
            tips += NutritionTip("⚠️", "Ai depășit obiectivul caloric în $highCal zile. Verifică porțiile și încearcă să reduci gustările.", TipSeverity.WARNING)
        }

        // WARNING: Low calories
        val lowCal = loggedDays.count { it.totalCalories < calorieTarget * 0.6 }
        if (lowCal >= 2) {
            tips += NutritionTip("📉", "Ai consumat prea puține calorii în $lowCal zile. Nu sări peste mese, corpul are nevoie de energie!", TipSeverity.WARNING)
        }

        // WARNING: Skipped breakfast
        val noBreakfast = loggedDays.count { day ->
            day.mealBreakdown[MealType.BREAKFAST]?.isEmpty() != false
        }
        if (noBreakfast >= 3) {
            tips += NutritionTip("🌅", "Ai sărit peste micul dejun de $noBreakfast ori. Micul dejun ajută la menținerea energiei și a concentrării.", TipSeverity.WARNING)
        }

        // WARNING: Too much fat
        val avgFat = loggedDays.map { it.totalFatG }.average().toFloat()
        if (avgFat > fatTarget * 1.3f) {
            tips += NutritionTip("🧈", "Consumul mediu de grăsimi a fost prea ridicat (${avgFat.roundToInt()}g/zi vs ${fatTarget}g obiectiv). Alege opțiuni mai slabe.", TipSeverity.WARNING)
        }

        // INFO: Low fiber
        val avgFiber = loggedDays.map { it.totalFiberG }.average().toFloat()
        if (avgFiber < 20f) {
            tips += NutritionTip("🥦", "Consumul de fibre a fost scăzut (${avgFiber.roundToInt()}g/zi). Adaugă mai multe legume, fructe și cereale integrale.", TipSeverity.INFO)
        }

        // INFO: Too many snacks
        val avgSnacks = loggedDays.map { day ->
            day.mealBreakdown[MealType.SNACK]?.size ?: 0
        }.average()
        if (avgSnacks > 2) {
            tips += NutritionTip("🍪", "Gustările au fost frecvente. Încearcă mese principale mai consistente pentru a reduce nevoia de gustări.", TipSeverity.INFO)
        }

        // INFO: Unbalanced carbs
        val avgCalories = loggedDays.map { it.totalCalories }.average()
        val avgCarbs = loggedDays.map { it.totalCarbsG }.average()
        val carbPercent = if (avgCalories > 0) (avgCarbs * 4 / avgCalories) else 0.0
        if (carbPercent > 0.6) {
            tips += NutritionTip("🍞", "Carbohidrații reprezintă ${(carbPercent * 100).roundToInt()}% din calorii. Echilibrează cu mai multe proteine și grăsimi sănătoase.", TipSeverity.INFO)
        }

        // INFO: High sugar
        val avgSugar = loggedDays.map { it.totalSugarG }.average().toFloat()
        if (avgSugar > 50f) {
            tips += NutritionTip("🍬", "Consumul mediu de zahăr a fost ridicat (${avgSugar.roundToInt()}g/zi). Încearcă să reduci dulciurile și sucurile.", TipSeverity.INFO)
        }

        // GOOD: Good protein
        val goodProtein = loggedDays.count { it.totalProteinG >= proteinTarget }
        if (goodProtein >= 5) {
            tips += NutritionTip("💪", "Aportul de proteine a fost excelent săptămâna aceasta! Continuă așa!", TipSeverity.GOOD)
        }

        // GOOD: Balanced meals
        val goodCal = loggedDays.count {
            it.totalCalories in (calorieTarget * 0.9).toInt()..(calorieTarget * 1.1).toInt()
        }
        if (goodCal >= 5) {
            tips += NutritionTip("🎯", "Ai respectat obiectivul caloric în $goodCal din $daysLogged zile. Bravo!", TipSeverity.GOOD)
        }

        // GOOD: Good variety
        val allMealsLogged = loggedDays.count { day ->
            day.mealBreakdown[MealType.BREAKFAST]?.isNotEmpty() == true &&
            day.mealBreakdown[MealType.LUNCH]?.isNotEmpty() == true &&
            day.mealBreakdown[MealType.DINNER]?.isNotEmpty() == true
        }
        if (allMealsLogged >= 5) {
            tips += NutritionTip("✅", "Ai mâncat regulat (mic dejun, prânz, cină) în $allMealsLogged zile. Excelent!", TipSeverity.GOOD)
        }

        return tips.sortedBy { when (it.severity) {
            TipSeverity.WARNING -> 0
            TipSeverity.INFO -> 1
            TipSeverity.GOOD -> 2
        } }.take(5)
    }
}
