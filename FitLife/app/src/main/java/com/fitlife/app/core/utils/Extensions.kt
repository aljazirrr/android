package com.fitlife.app.core.utils

import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

fun Long.toDateString(pattern: String = "dd MMM yyyy"): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(Date(this))
}

fun Long.toTimeString(): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(this))
}

fun Long.isToday(): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = this@isToday }
    val cal2 = Calendar.getInstance()
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

fun Long.startOfDay(): Long {
    val cal = Calendar.getInstance().apply { timeInMillis = this@startOfDay }
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

fun Long.endOfDay(): Long {
    val cal = Calendar.getInstance().apply { timeInMillis = this@endOfDay }
    cal.set(Calendar.HOUR_OF_DAY, 23)
    cal.set(Calendar.MINUTE, 59)
    cal.set(Calendar.SECOND, 59)
    cal.set(Calendar.MILLISECOND, 999)
    return cal.timeInMillis
}

fun Long.startOfWeek(): Long {
    val cal = Calendar.getInstance().apply {
        timeInMillis = this@startOfWeek
        set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
    }
    return cal.timeInMillis.startOfDay()
}

fun Long.endOfWeek(): Long {
    val cal = Calendar.getInstance().apply {
        timeInMillis = this@endOfWeek
        set(Calendar.DAY_OF_WEEK, firstDayOfWeek + 6)
    }
    return cal.timeInMillis.endOfDay()
}

fun Int.secondsToFormattedTime(): String {
    val hours = this / 3600
    val minutes = (this % 3600) / 60
    val secs = this % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, secs)
    } else {
        String.format("%d:%02d", minutes, secs)
    }
}

fun Float.toBmi(heightCm: Float): Float {
    val heightM = heightCm / 100f
    return (this / (heightM * heightM) * 10f).roundToInt() / 10f
}

fun Float.toBmiCategory(): String = when {
    this < 18.5f -> "Subponderal"
    this < 25f -> "Normal"
    this < 30f -> "Supraponderal"
    else -> "Obezitate"
}

fun Int.toCaloriesColor(): Long = when {
    this < 1500 -> 0xFF2196F3  // Blue - too low
    this < 1800 -> 0xFF4CAF50  // Green - good
    this < 2200 -> 0xFF8BC34A  // Light green - ok
    this < 2500 -> 0xFFFF9800  // Orange - high
    else -> 0xFFF44336         // Red - too high
}

fun String.isValidEmail(): Boolean =
    android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String.isValidPassword(): Boolean =
    length >= 8 && any { it.isUpperCase() } && any { it.isDigit() }

fun generateUid(): String = UUID.randomUUID().toString()
