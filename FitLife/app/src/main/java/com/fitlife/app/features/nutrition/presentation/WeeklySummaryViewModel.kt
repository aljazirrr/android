package com.fitlife.app.features.nutrition.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitlife.app.core.domain.model.NutritionLog
import com.fitlife.app.core.utils.MealType
import com.fitlife.app.core.utils.startOfDay
import com.fitlife.app.core.utils.startOfWeek
import com.fitlife.app.core.utils.endOfWeek
import com.fitlife.app.core.utils.toDateString
import com.fitlife.app.features.auth.domain.repository.AuthRepository
import com.fitlife.app.features.nutrition.domain.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToInt

data class WeeklySummaryUiState(
    val isLoading: Boolean = true,
    val summary: WeeklySummaryData? = null,
    val weekStartDate: Long = System.currentTimeMillis().startOfWeek(),
    val error: String? = null
)

@HiltViewModel
class WeeklySummaryViewModel @Inject constructor(
    private val nutritionRepository: NutritionRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeeklySummaryUiState())
    val uiState: StateFlow<WeeklySummaryUiState> = _uiState.asStateFlow()

    private val targets = MacroTargets(
        calories = 2000,
        proteinG = 150,
        carbsG = 250,
        fatG = 65,
        waterMl = 2500
    )

    init {
        loadWeekData()
    }

    fun previousWeek() {
        val cal = Calendar.getInstance().apply {
            timeInMillis = _uiState.value.weekStartDate
            add(Calendar.DAY_OF_YEAR, -7)
        }
        _uiState.update { it.copy(weekStartDate = cal.timeInMillis.startOfWeek()) }
        loadWeekData()
    }

    fun nextWeek() {
        val nextStart = Calendar.getInstance().apply {
            timeInMillis = _uiState.value.weekStartDate
            add(Calendar.DAY_OF_YEAR, 7)
        }.timeInMillis.startOfWeek()

        if (nextStart > System.currentTimeMillis().startOfWeek()) return

        _uiState.update { it.copy(weekStartDate = nextStart) }
        loadWeekData()
    }

    private fun loadWeekData() {
        val userId = authRepository.currentUser?.uid ?: return
        val weekStart = _uiState.value.weekStartDate
        val weekEnd = weekStart.endOfWeek()

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            nutritionRepository.getNutritionLogsInRange(userId, weekStart, weekEnd)
                .collect { logs ->
                    val summary = buildWeeklySummary(logs, weekStart, weekEnd)
                    _uiState.update {
                        it.copy(isLoading = false, summary = summary, error = null)
                    }
                }
        }
    }

    private fun buildWeeklySummary(
        logs: List<NutritionLog>,
        weekStart: Long,
        weekEnd: Long
    ): WeeklySummaryData {
        val dayLabels = listOf("Luni", "Marți", "Miercuri", "Joi", "Vineri", "Sâmbătă", "Duminică")
        val logsByDay = logs.associateBy { it.date.startOfDay() }

        val dailyLogs = (0..6).map { dayOffset ->
            val cal = Calendar.getInstance().apply {
                timeInMillis = weekStart
                add(Calendar.DAY_OF_YEAR, dayOffset)
            }
            val dayDate = cal.timeInMillis.startOfDay()
            val log = logsByDay[dayDate]

            val mealTypes = MealType.entries.associateWith { type ->
                log?.meals?.any { it.type == type } ?: false
            }

            DaySummary(
                date = dayDate,
                dayOfWeekLabel = dayLabels[dayOffset % 7],
                calories = log?.totalCalories ?: 0,
                proteinG = log?.totalProteinG ?: 0f,
                carbsG = log?.totalCarbsG ?: 0f,
                fatG = log?.totalFatG ?: 0f,
                fiberG = log?.totalFiberG ?: 0f,
                waterMl = log?.waterMl ?: 0,
                mealCount = log?.meals?.size ?: 0,
                hasMealType = mealTypes
            )
        }

        val loggedDays = dailyLogs.filter { it.calories > 0 }
        val daysLogged = loggedDays.size

        val averages = if (daysLogged > 0) {
            MacroAverages(
                calories = loggedDays.sumOf { it.calories } / daysLogged,
                proteinG = loggedDays.map { it.proteinG }.average().toFloat(),
                carbsG = loggedDays.map { it.carbsG }.average().toFloat(),
                fatG = loggedDays.map { it.fatG }.average().toFloat(),
                fiberG = loggedDays.map { it.fiberG }.average().toFloat()
            )
        } else {
            MacroAverages(0, 0f, 0f, 0f, 0f)
        }

        val bestDay = loggedDays.minByOrNull { abs(it.calories - targets.calories) }
        val worstDay = loggedDays.maxByOrNull { abs(it.calories - targets.calories) }

        val totalWater = dailyLogs.sumOf { it.waterMl }
        val avgWater = if (daysLogged > 0) totalWater / daysLogged else 0

        val tips = generateTips(dailyLogs, daysLogged, targets)

        return WeeklySummaryData(
            weekStartDate = weekStart,
            weekEndDate = weekEnd,
            dailyLogs = dailyLogs,
            averages = averages,
            targets = targets,
            bestDay = bestDay,
            worstDay = if (worstDay != bestDay) worstDay else null,
            totalWaterMl = totalWater,
            avgWaterMl = avgWater,
            daysLogged = daysLogged,
            tips = tips
        )
    }

    private fun generateTips(
        dailyLogs: List<DaySummary>,
        daysLogged: Int,
        targets: MacroTargets
    ): List<NutritionTip> {
        if (daysLogged == 0) return emptyList()

        val tips = mutableListOf<NutritionTip>()
        val loggedDays = dailyLogs.filter { it.calories > 0 }

        // WARNING: Low protein days
        val lowProteinDays = loggedDays.count { it.proteinG < targets.proteinG * 0.8f }
        if (lowProteinDays >= 2) {
            tips += NutritionTip(
                icon = "🥩",
                message = "Ai consumat prea puține proteine în $lowProteinDays din $daysLogged zile. Încearcă să adaugi mai multă carne, ouă sau lactate.",
                severity = TipSeverity.WARNING
            )
        }

        // WARNING: High calorie days
        val highCalDays = loggedDays.count { it.calories > targets.calories * 1.2 }
        if (highCalDays >= 2) {
            tips += NutritionTip(
                icon = "⚠️",
                message = "Ai depășit obiectivul caloric în $highCalDays zile. Verifică porțiile și încearcă să reduci gustările.",
                severity = TipSeverity.WARNING
            )
        }

        // WARNING: Low calorie days
        val lowCalDays = loggedDays.count { it.calories < targets.calories * 0.6 }
        if (lowCalDays >= 2) {
            tips += NutritionTip(
                icon = "📉",
                message = "Ai consumat prea puține calorii în $lowCalDays zile. Nu sări peste mese, corpul are nevoie de energie!",
                severity = TipSeverity.WARNING
            )
        }

        // WARNING: Skipped breakfast
        val skippedBreakfast = loggedDays.count { it.hasMealType[MealType.BREAKFAST] != true }
        if (skippedBreakfast >= 3) {
            tips += NutritionTip(
                icon = "🌅",
                message = "Ai sărit peste micul dejun de $skippedBreakfast ori. Micul dejun ajută la menținerea energiei și a concentrării.",
                severity = TipSeverity.WARNING
            )
        }

        // WARNING: Low water
        val lowWaterDays = loggedDays.count { it.waterMl < targets.waterMl * 0.7 }
        if (lowWaterDays >= 3) {
            tips += NutritionTip(
                icon = "💧",
                message = "Hidratarea a fost sub obiectiv în $lowWaterDays din $daysLogged zile. Încearcă să bei apă regulat pe parcursul zilei.",
                severity = TipSeverity.WARNING
            )
        }

        // WARNING: Too much fat
        val avgFat = loggedDays.map { it.fatG }.average().toFloat()
        if (avgFat > targets.fatG * 1.3f) {
            tips += NutritionTip(
                icon = "🧈",
                message = "Consumul mediu de grăsimi a fost prea ridicat (${avgFat.roundToInt()}g/zi vs ${targets.fatG}g obiectiv). Alege opțiuni mai slabe.",
                severity = TipSeverity.WARNING
            )
        }

        // INFO: Low fiber
        val avgFiber = loggedDays.map { it.fiberG }.average().toFloat()
        if (avgFiber < 20f) {
            tips += NutritionTip(
                icon = "🥦",
                message = "Consumul de fibre a fost scăzut (${avgFiber.roundToInt()}g/zi). Adaugă mai multe legume, fructe și cereale integrale.",
                severity = TipSeverity.INFO
            )
        }

        // INFO: Too many snacks
        val avgSnacks = loggedDays.map { day ->
            day.hasMealType.count { (type, has) -> type == MealType.SNACK && has }
        }.average()
        if (avgSnacks > 2) {
            tips += NutritionTip(
                icon = "🍪",
                message = "Gustările au fost frecvente. Încearcă mese principale mai consistente pentru a reduce nevoia de gustări.",
                severity = TipSeverity.INFO
            )
        }

        // INFO: Unbalanced carbs
        val avgCalories = loggedDays.map { it.calories }.average()
        val avgCarbs = loggedDays.map { it.carbsG }.average()
        val carbCalPercent = if (avgCalories > 0) (avgCarbs * 4 / avgCalories) else 0.0
        if (carbCalPercent > 0.6) {
            tips += NutritionTip(
                icon = "🍞",
                message = "Carbohidrații reprezintă ${(carbCalPercent * 100).roundToInt()}% din calorii. Echilibrează cu mai multe proteine și grăsimi sănătoase.",
                severity = TipSeverity.INFO
            )
        }

        // GOOD: Good protein
        val goodProteinDays = loggedDays.count { it.proteinG >= targets.proteinG }
        if (goodProteinDays >= 5) {
            tips += NutritionTip(
                icon = "💪",
                message = "Aportul de proteine a fost excelent săptămâna aceasta! Continuă așa!",
                severity = TipSeverity.GOOD
            )
        }

        // GOOD: Good hydration
        val goodWaterDays = loggedDays.count { it.waterMl >= targets.waterMl }
        if (goodWaterDays >= 5) {
            tips += NutritionTip(
                icon = "🏆",
                message = "Hidratare excelentă! Ai atins obiectivul de apă în $goodWaterDays zile.",
                severity = TipSeverity.GOOD
            )
        }

        // GOOD: Good calorie adherence
        val goodCalDays = loggedDays.count {
            it.calories >= targets.calories * 0.9 && it.calories <= targets.calories * 1.1
        }
        if (goodCalDays >= 5) {
            tips += NutritionTip(
                icon = "🎯",
                message = "Ai respectat obiectivul caloric în $goodCalDays din $daysLogged zile. Bravo!",
                severity = TipSeverity.GOOD
            )
        }

        return tips
            .sortedBy { tip ->
                when (tip.severity) {
                    TipSeverity.WARNING -> 0
                    TipSeverity.INFO -> 1
                    TipSeverity.GOOD -> 2
                }
            }
            .take(5)
    }
}
