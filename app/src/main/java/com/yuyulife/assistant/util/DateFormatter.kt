package com.yuyulife.assistant.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

fun formatTimestamp(timestamp: Long): String = SimpleDateFormat(
    "MM月dd日 HH:mm",
    Locale.CHINA,
).format(Date(timestamp))

fun formatDate(timestamp: Long): String = SimpleDateFormat(
    "yyyy年M月d日",
    Locale.CHINA,
).format(Date(timestamp))

fun formatMonth(timestamp: Long): String = SimpleDateFormat(
    "yyyy年M月",
    Locale.CHINA,
).format(Date(timestamp))

fun startOfDay(timestamp: Long): Long = calendarAt(timestamp).apply {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

fun startOfMonth(timestamp: Long): Long = calendarAt(timestamp).apply {
    set(Calendar.DAY_OF_MONTH, 1)
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

fun shiftDays(timestamp: Long, amount: Int): Long = calendarAt(startOfDay(timestamp)).apply {
    add(Calendar.DAY_OF_MONTH, amount)
}.timeInMillis

fun shiftMonths(timestamp: Long, amount: Int): Long = calendarAt(startOfMonth(timestamp)).apply {
    add(Calendar.MONTH, amount)
}.timeInMillis

fun endOfSelectedDayExclusive(timestamp: Long): Long = shiftDays(timestamp, 1)

fun endOfSelectedMonthExclusive(timestamp: Long): Long = shiftMonths(timestamp, 1)

fun dateWithCurrentTime(selectedDate: Long, now: Long = System.currentTimeMillis()): Long {
    val date = calendarAt(selectedDate)
    val current = calendarAt(now)
    date.set(Calendar.HOUR_OF_DAY, current.get(Calendar.HOUR_OF_DAY))
    date.set(Calendar.MINUTE, current.get(Calendar.MINUTE))
    date.set(Calendar.SECOND, current.get(Calendar.SECOND))
    date.set(Calendar.MILLISECOND, current.get(Calendar.MILLISECOND))
    return date.timeInMillis
}

fun isDeadlineOverdue(deadlineAt: Long, now: Long = System.currentTimeMillis()): Boolean =
    startOfDay(deadlineAt) < startOfDay(now)

fun dateParts(timestamp: Long): Triple<Int, Int, Int> = calendarAt(timestamp).let {
    Triple(it.get(Calendar.YEAR), it.get(Calendar.MONTH), it.get(Calendar.DAY_OF_MONTH))
}

fun monthTimestamp(year: Int, month: Int): Long = Calendar.getInstance().apply {
    clear()
    set(year, month, 1, 0, 0, 0)
}.timeInMillis

private fun calendarAt(timestamp: Long): Calendar = Calendar.getInstance().apply {
    timeInMillis = timestamp
}
