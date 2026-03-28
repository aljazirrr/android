package com.fooddiary.app.ui.screens.summary

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
import com.fooddiary.app.domain.model.*
import com.fooddiary.app.ui.screens.diary.toDateString
import com.fooddiary.app.ui.theme.*
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

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val summary = uiState.summary
            if (summary == null || summary.daysLogged == 0) {
                Column(modifier = Modifier.fillMaxSize()) {
                    WeekSelector(uiState.weekStart, viewModel::previousWeek, viewModel::nextWeek)
                    Spacer(Modifier.weight(1f))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        Spacer(Modifier.height(16.dp))
                        Text("Nicio înregistrare", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Nu ai înregistrat mese în această săptămână.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
                    }
                    Spacer(Modifier.weight(1f))
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { WeekSelector(uiState.weekStart, viewModel::previousWeek, viewModel::nextWeek) }
                    item { CalorieChartCard(summary) }
                    item { MacrosTrendCard(summary) }
                    item { AveragesCard(summary) }
                    item { BestWorstCard(summary) }
                    if (summary.tips.isNotEmpty()) {
                        item {
                            Text(
                                "Sfaturi pentru săptămâna viitoare",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                        items(summary.tips) { tip -> TipCard(tip) }
                    }
                }
            }
        }
    }
}

// ─── Week Selector ───────────────────────────────────────────────────────────

@Composable
private fun WeekSelector(weekStart: Long, onPrev: () -> Unit, onNext: () -> Unit) {
    val weekEnd = weekStart + 6 * 86400000L
    val currentWeekStart = com.fooddiary.app.ui.screens.diary.startOfWeek(System.currentTimeMillis())
    val isCurrentWeek = weekStart == currentWeekStart

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrev) { Icon(Icons.Default.ChevronLeft, null) }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "${weekStart.toDateString("d MMM")} - ${weekEnd.toDateString("d MMM")}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (isCurrentWeek) {
                Text("Săptămâna curentă", style = MaterialTheme.typography.bodySmall, color = Green)
            }
        }
        IconButton(onClick = onNext, enabled = !isCurrentWeek) {
            Icon(Icons.Default.ChevronRight, null,
                tint = if (isCurrentWeek) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f) else LocalContentColor.current)
        }
    }
}

// ─── Calorie Bar Chart ───────────────────────────────────────────────────────

@Composable
private fun CalorieChartCard(summary: WeeklySummary) {
    val dayLabels = summary.days.map { it.dayLabel.take(2) }
    val modelProducer = remember { ChartEntryModelProducer() }

    LaunchedEffect(summary) {
        modelProducer.setEntries(
            summary.days.mapIndexed { i, d -> entryOf(i.toFloat(), d.totalCalories.toFloat()) }
        )
    }

    val formatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
        dayLabels.getOrElse(value.toInt()) { "" }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Calorii zilnice", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Chart(
                chart = columnChart(
                    columns = listOf(
                        lineComponent(
                            color = Green,
                            thickness = 16.dp,
                            shape = com.patrykandpatrick.vico.core.component.shape.Shapes.roundedCornerShape(topLeftPercent = 40, topRightPercent = 40)
                        )
                    )
                ),
                chartModelProducer = modelProducer,
                startAxis = startAxis(),
                bottomAxis = bottomAxis(valueFormatter = formatter),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
        }
    }
}

// ─── Macros Line Chart ───────────────────────────────────────────────────────

@Composable
private fun MacrosTrendCard(summary: WeeklySummary) {
    val dayLabels = summary.days.map { it.dayLabel.take(2) }
    val modelProducer = remember { ChartEntryModelProducer() }

    LaunchedEffect(summary) {
        modelProducer.setEntries(
            summary.days.mapIndexed { i, d -> entryOf(i.toFloat(), d.totalProteinG) },
            summary.days.mapIndexed { i, d -> entryOf(i.toFloat(), d.totalCarbsG) },
            summary.days.mapIndexed { i, d -> entryOf(i.toFloat(), d.totalFatG) }
        )
    }

    val formatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
        dayLabels.getOrElse(value.toInt()) { "" }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Tendință macronutrienți", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LegendDot("Proteine", Color(0xFF4CAF50))
                LegendDot("Carbohidrați", Blue)
                LegendDot("Grăsimi", Orange)
            }
            Spacer(Modifier.height(12.dp))
            Chart(
                chart = lineChart(
                    lines = listOf(
                        lineSpec(lineColor = Color(0xFF4CAF50)),
                        lineSpec(lineColor = Blue),
                        lineSpec(lineColor = Orange)
                    )
                ),
                chartModelProducer = modelProducer,
                startAxis = startAxis(),
                bottomAxis = bottomAxis(valueFormatter = formatter),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
        }
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(color))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

// ─── Averages Card ───────────────────────────────────────────────────────────

@Composable
private fun AveragesCard(summary: WeeklySummary) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Medii zilnice", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text("Bazat pe ${summary.daysLogged} zile înregistrate", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))

            Text("${summary.avgCalories} kcal/zi", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Green)
            Spacer(Modifier.height(12.dp))

            MacroRow("Proteine", summary.avgProteinG, 150f, Color(0xFF4CAF50))
            MacroRow("Carbohidrați", summary.avgCarbsG, 250f, Blue)
            MacroRow("Grăsimi", summary.avgFatG, 65f, Orange)
            MacroRow("Fibre", summary.avgFiberG, 25f, Purple)
        }
    }
}

@Composable
private fun MacroRow(name: String, current: Float, target: Float, color: Color) {
    val progress = (current / target).coerceIn(0f, 1f)
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(name, style = MaterialTheme.typography.bodySmall)
            Text("${current.roundToInt()}g / ${target.roundToInt()}g", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}

// ─── Best / Worst Day ────────────────────────────────────────────────────────

@Composable
private fun BestWorstCard(summary: WeeklySummary) {
    if (summary.bestDay == null && summary.worstDay == null) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Performanță", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                summary.bestDay?.let { day ->
                    DayCard("Cel mai bun", day, Green, Modifier.weight(1f))
                }
                summary.worstDay?.let { day ->
                    DayCard("Cel mai slab", day, Red, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DayCard(label: String, day: DaySummary, color: Color, modifier: Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(day.dayLabel, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(day.date.toDateString("d MMM"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text("${day.totalCalories} kcal", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = color)
        }
    }
}

// ─── Tip Card ────────────────────────────────────────────────────────────────

@Composable
private fun TipCard(tip: NutritionTip) {
    val color = when (tip.severity) {
        TipSeverity.WARNING -> Orange
        TipSeverity.INFO -> Blue
        TipSeverity.GOOD -> Green
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f))
    ) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
            Box(Modifier.width(4.dp).height(36.dp).clip(RoundedCornerShape(2.dp)).background(color))
            Text(tip.icon, fontSize = 22.sp)
            Column(Modifier.weight(1f)) {
                Text(
                    when (tip.severity) {
                        TipSeverity.WARNING -> "Atenție"
                        TipSeverity.INFO -> "Sugestie"
                        TipSeverity.GOOD -> "Foarte bine!"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Spacer(Modifier.height(2.dp))
                Text(tip.message, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
