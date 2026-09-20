package com.yuyulife.assistant.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val MEMO_TIME_GAP_MILLIS = 5 * 60 * 1000L

fun shouldShowMemoTimestamp(previous: Long?, current: Long): Boolean {
    if (previous == null) return true
    if (!isSameCalendarDay(previous, current)) return true
    return current - previous >= MEMO_TIME_GAP_MILLIS
}

fun formatMemoTimestamp(timestamp: Long): String =
    SimpleDateFormat("yyyy年M月d日 HH:mm", Locale.getDefault()).format(Date(timestamp))

fun formatMemoExactTimestamp(timestamp: Long): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))

fun formatMemoThreadTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    return when {
        isSameCalendarDay(now, timestamp) ->
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
        isSameCalendarYear(now, timestamp) ->
            SimpleDateFormat("M月d日", Locale.getDefault()).format(Date(timestamp))
        else -> SimpleDateFormat("yyyy/M/d", Locale.getDefault()).format(Date(timestamp))
    }
}

private fun isSameCalendarDay(first: Long, second: Long): Boolean {
    val firstCalendar = Calendar.getInstance().apply { timeInMillis = first }
    val secondCalendar = Calendar.getInstance().apply { timeInMillis = second }
    return firstCalendar.get(Calendar.ERA) == secondCalendar.get(Calendar.ERA) &&
        firstCalendar.get(Calendar.YEAR) == secondCalendar.get(Calendar.YEAR) &&
        firstCalendar.get(Calendar.DAY_OF_YEAR) == secondCalendar.get(Calendar.DAY_OF_YEAR)
}

private fun isSameCalendarYear(first: Long, second: Long): Boolean {
    val firstCalendar = Calendar.getInstance().apply { timeInMillis = first }
    val secondCalendar = Calendar.getInstance().apply { timeInMillis = second }
    return firstCalendar.get(Calendar.ERA) == secondCalendar.get(Calendar.ERA) &&
        firstCalendar.get(Calendar.YEAR) == secondCalendar.get(Calendar.YEAR)
}
