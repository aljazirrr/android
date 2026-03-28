package com.fitlife.app.features.nutrition.presentation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitlife.app.core.ui.components.CircularProgressRing
import com.fitlife.app.core.ui.components.EmptyState
import com.fitlife.app.core.ui.components.LoadingOverlay
import com.fitlife.app.core.ui.components.SectionHeader
import com.fitlife.app.core.ui.theme.*
import com.fitlife.app.core.utils.toDateString
import com.fitlife.app.features.nutrition.domain.*
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.line.lineSpec
import com.patrykandpatrick.vico.compose.component.lineComponent
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklySummaryScreen(
    onNavigateBack: () -> Unit,
    viewModel: WeeklySummaryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Rezumat Săptămânal", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Înapoi")
                }
            }
        )

        LoadingOverlay(isVisible = uiState.isLoading)

        if (!uiState.isLoading) {
            val summary = uiState.summary

            if (summary == null || summary.daysLogged == 0) {
                Column(modifier = Modifier.fillMaxSize()) {
                    WeekSelector(
                        weekStart = uiState.weekStartDate,
                        onPrevious = viewModel::previousWeek,
                        onNext = viewModel::nextWeek
                    )
                    Spacer(Modifier.weight(1f))
                    EmptyState(
                        icon = Icons.Default.CalendarMonth,
                        title = "Nicio înregistrare",
                        description = "Nu ai înregistrat mese în această săptămână. Începe să adaugi alimente pentru a vedea rezumatul."
                    )
                    Spacer(Modifier.weight(1f))
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        WeekSelector(
                            weekStart = uiState.weekStartDate,
                            onPrevious = viewModel::previousWeek,
                            onNext = viewModel::nextWeek
                        )
                    }

                    item { CalorieChartCard(summary) }

                    item { MacrosTrendCard(summary) }

                    item { AveragesCard(summary) }

                    item { BestWorstDayCard(summary) }

                    item { WaterWeeklyCard(summary) }

                    if (summary.tips.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "Sfaturi pentru săptămâna viitoare",
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                        items(summary.tips) { tip ->
                            TipCard(tip)
                        }
                    }
                }
            }
        }
    }
}

// ─── Week Selector ───────────────────────────────────────────────────────────

@Composable
private fun WeekSelector(weekStart: Long, onPrevious: () -> Unit, onNext: () -> Unit) {
    val weekEnd = weekStart + 6 * 86400000L
    val isCurrentWeek = System.currentTimeMillis() in weekStart..weekEnd + 86400000L

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Default.ChevronLeft, null)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "${weekStart.toDateString("d MMM")} - ${weekEnd.toDateString("d MMM")}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (isCurrentWeek) {
                Text(
                    "Săptămâna curentă",
                    style = MaterialTheme.typography.bodySmall,
                    color = FitGreen
                )
            }
        }
        IconButton(
            onClick = onNext,
            enabled = !isCurrentWeek
        ) {
            Icon(
                Icons.Default.ChevronRight, null,
                tint = if (isCurrentWeek)
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                else LocalContentColor.current
            )
        }
    }
}

// ─── Calorie Bar Chart ───────────────────────────────────────────────────────

@Composable
private fun CalorieChartCard(summary: WeeklySummaryData) {
    val dayLabels = summary.dailyLogs.map { it.dayOfWeekLabel.take(2) }
    val modelProducer = remember { ChartEntryModelProducer() }

    LaunchedEffect(summary) {
        modelProducer.setEntries(
            summary.dailyLogs.mapIndexed { index, day ->
                entryOf(index.toFloat(), day.calories.toFloat())
            }
        )
    }

    val bottomAxisFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
        dayLabels.getOrElse(value.toInt()) { "" }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Calorii zilnice",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Obiectiv: ${summary.targets.calories} kcal/zi",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))

            Chart(
                chart = columnChart(
                    columns = listOf(
                        lineComponent(
                            color = FitGreen,
                            thickness = 16.dp,
                            shape = com.patrykandpatrick.vico.core.component.shape.Shapes.roundedCornerShape(
                                topLeftPercent = 40,
                                topRightPercent = 40
                            )
                        )
                    )
                ),
                chartModelProducer = modelProducer,
                startAxis = startAxis(),
                bottomAxis = bottomAxis(valueFormatter = bottomAxisFormatter),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}

// ─── Macros Trend Chart ──────────────────────────────────────────────────────

@Composable
private fun MacrosTrendCard(summary: WeeklySummaryData) {
    val dayLabels = summary.dailyLogs.map { it.dayOfWeekLabel.take(2) }
    val modelProducer = remember { ChartEntryModelProducer() }

    LaunchedEffect(summary) {
        val proteinEntries = summary.dailyLogs.mapIndexed { i, d -> entryOf(i.toFloat(), d.proteinG) }
        val carbsEntries = summary.dailyLogs.mapIndexed { i, d -> entryOf(i.toFloat(), d.carbsG) }
        val fatEntries = summary.dailyLogs.mapIndexed { i, d -> entryOf(i.toFloat(), d.fatG) }
        modelProducer.setEntries(proteinEntries, carbsEntries, fatEntries)
    }

    val bottomAxisFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
        dayLabels.getOrElse(value.toInt()) { "" }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Tendință macronutrienți",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LegendItem("Proteine", Color(0xFF4CAF50))
                LegendItem("Carbohidrați", FitBlue)
                LegendItem("Grăsimi", FitOrange)
            }

            Spacer(Modifier.height(16.dp))

            Chart(
                chart = lineChart(
                    lines = listOf(
                        lineSpec(lineColor = Color(0xFF4CAF50)),
                        lineSpec(lineColor = FitBlue),
                        lineSpec(lineColor = FitOrange)
                    )
                ),
                chartModelProducer = modelProducer,
                startAxis = startAxis(),
                bottomAxis = bottomAxis(valueFormatter = bottomAxisFormatter),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

// ─── Averages Card ───────────────────────────────────────────────────────────

@Composable
private fun AveragesCard(summary: WeeklySummaryData) {
    val avg = summary.averages
    val targets = summary.targets
    val calProgress = (avg.calories.toFloat() / targets.calories).coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Medii zilnice",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Bazat pe ${summary.daysLogged} zile înregistrate",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressRing(
                    progress = calProgress,
                    size = 90.dp,
                    strokeWidth = 9.dp,
                    color = FitGreen
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${avg.calories}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = FitGreen
                        )
                        Text("kcal/zi", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    MacroProgressRow("Proteine", avg.proteinG, targets.proteinG.toFloat(), Color(0xFF4CAF50))
                    MacroProgressRow("Carbohidrați", avg.carbsG, targets.carbsG.toFloat(), FitBlue)
                    MacroProgressRow("Grăsimi", avg.fatG, targets.fatG.toFloat(), FitOrange)
                    MacroProgressRow("Fibre", avg.fiberG, 25f, FitPurple)
                }
            }
        }
    }
}

@Composable
private fun MacroProgressRow(name: String, current: Float, target: Float, color: Color) {
    val progress = (current / target).coerceIn(0f, 1f)
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(name, style = MaterialTheme.typography.labelSmall)
            Text(
                "${current.roundToInt()}g / ${target.roundToInt()}g",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}

// ─── Best / Worst Day ────────────────────────────────────────────────────────

@Composable
private fun BestWorstDayCard(summary: WeeklySummaryData) {
    val best = summary.bestDay
    val worst = summary.worstDay

    if (best == null && worst == null) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Performanță",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (best != null) {
                    DayHighlightCard(
                        label = "Cel mai bun",
                        day = best,
                        target = summary.targets.calories,
                        color = FitGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (worst != null) {
                    DayHighlightCard(
                        label = "Cel mai slab",
                        day = worst,
                        target = summary.targets.calories,
                        color = FitRed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DayHighlightCard(
    label: String,
    day: DaySummary,
    target: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    val percent = if (target > 0) (day.calories * 100 / target) else 0

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(day.dayOfWeekLabel, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(day.date.toDateString("d MMM"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Text("${day.calories} kcal", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = color)
            Text("$percent% din obiectiv", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ─── Water Weekly Card ───────────────────────────────────────────────────────

@Composable
private fun WaterWeeklyCard(summary: WeeklySummaryData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = FitBlue.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("💧", fontSize = 24.sp)
                Column {
                    Text(
                        "Hidratare săptămânală",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Medie: ${summary.avgWaterMl}ml/zi • Total: ${summary.totalWaterMl}ml",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            summary.dailyLogs.forEach { day ->
                val progress = (day.waterMl.toFloat() / summary.targets.waterMl).coerceIn(0f, 1f)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        day.dayOfWeekLabel.take(2),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.width(24.dp),
                        fontWeight = FontWeight.Bold
                    )
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (progress >= 1f) FitGreen else FitBlue,
                        trackColor = FitBlue.copy(alpha = 0.2f)
                    )
                    Text(
                        "${day.waterMl}ml",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.width(50.dp),
                        textAlign = TextAlign.End,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ─── Tip Card ────────────────────────────────────────────────────────────────

@Composable
private fun TipCard(tip: NutritionTip) {
    val borderColor = when (tip.severity) {
        TipSeverity.WARNING -> FitOrange
        TipSeverity.INFO -> FitBlue
        TipSeverity.GOOD -> FitGreen
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = borderColor.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(borderColor)
            )
            Text(tip.icon, fontSize = 24.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    when (tip.severity) {
                        TipSeverity.WARNING -> "Atenție"
                        TipSeverity.INFO -> "Sugestie"
                        TipSeverity.GOOD -> "Foarte bine!"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = borderColor
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    tip.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
