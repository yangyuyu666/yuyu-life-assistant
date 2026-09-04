package com.yuyulife.assistant.reminder

import com.yuyulife.assistant.domain.model.ReminderMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderSchedulePlannerTest {
    private val now = 1_000_000L
    private val deadline = now + 60 * 60_000L

    @Test
    fun repeatedMode_plansThreeFiveMinuteOccurrences() {
        val reminders = ReminderSchedulePlanner.plan(
            deadline = deadline,
            now = now,
            leadMinutes = 30,
            requestedMode = ReminderMode.REPEATED,
            exactAlarmAllowed = true,
            fullScreenAllowed = true,
        )

        assertEquals(3, reminders.size)
        assertEquals(5 * 60_000L, reminders[1].triggerAt - reminders[0].triggerAt)
        assertEquals(ReminderMode.REPEATED, reminders.first().mode)
    }

    @Test
    fun missingRequiredPermission_fallsBackToOneNormalReminder() {
        val reminders = ReminderSchedulePlanner.plan(
            deadline = deadline,
            now = now,
            leadMinutes = 30,
            requestedMode = ReminderMode.ALARM,
            exactAlarmAllowed = true,
            fullScreenAllowed = false,
        )

        assertEquals(1, reminders.size)
        assertEquals(ReminderMode.NORMAL, reminders.single().mode)
    }

    @Test
    fun elapsedLeadTime_schedulesImmediatelyWhenDeadlineIsFuture() {
        val reminders = ReminderSchedulePlanner.plan(
            deadline = now + 5_000L,
            now = now,
            leadMinutes = 30,
            requestedMode = ReminderMode.ENHANCED,
            exactAlarmAllowed = true,
            fullScreenAllowed = true,
        )

        assertEquals(now + 1_000L, reminders.single().triggerAt)
    }

    @Test
    fun disabledOrPastTodo_hasNoReminder() {
        assertTrue(
            ReminderSchedulePlanner.plan(deadline, now, 30, ReminderMode.OFF, true, true).isEmpty(),
        )
        assertTrue(
            ReminderSchedulePlanner.plan(now, now, 30, ReminderMode.NORMAL, true, true).isEmpty(),
        )
    }
}
