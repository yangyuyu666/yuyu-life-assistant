package com.yuyulife.assistant.util

import java.util.Calendar
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MemoTimeFormatterTest {
    @Test
    fun firstMessageAlwaysShowsTimestamp() {
        assertTrue(shouldShowMemoTimestamp(previous = null, current = 1_000L))
    }

    @Test
    fun messagesLessThanFiveMinutesApartShareTimestamp() {
        val start = localTime(2026, Calendar.SEPTEMBER, 20, 10, 0)
        assertFalse(shouldShowMemoTimestamp(start, start + 4 * 60 * 1000L + 59_000L))
    }

    @Test
    fun fiveMinuteGapShowsTimestamp() {
        val start = localTime(2026, Calendar.SEPTEMBER, 20, 10, 0)
        assertTrue(shouldShowMemoTimestamp(start, start + 5 * 60 * 1000L))
    }

    @Test
    fun newCalendarDayShowsTimestampEvenWithShortGap() {
        val beforeMidnight = localTime(2026, Calendar.SEPTEMBER, 20, 23, 59)
        val afterMidnight = localTime(2026, Calendar.SEPTEMBER, 21, 0, 1)
        assertTrue(shouldShowMemoTimestamp(beforeMidnight, afterMidnight))
    }

    private fun localTime(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long =
        Calendar.getInstance().apply {
            clear()
            set(year, month, day, hour, minute)
        }.timeInMillis
}
