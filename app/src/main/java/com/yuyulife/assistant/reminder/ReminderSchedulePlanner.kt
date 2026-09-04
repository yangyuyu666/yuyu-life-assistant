package com.yuyulife.assistant.reminder

import com.yuyulife.assistant.domain.model.ReminderMode

data class PlannedReminder(
    val triggerAt: Long,
    val mode: ReminderMode,
    val occurrence: Int,
)

object ReminderSchedulePlanner {
    const val REPEAT_COUNT = 3
    const val REPEAT_INTERVAL_MILLIS = 5 * 60_000L

    fun plan(
        deadline: Long,
        now: Long,
        leadMinutes: Int,
        requestedMode: ReminderMode,
        exactAlarmAllowed: Boolean,
        fullScreenAllowed: Boolean,
    ): List<PlannedReminder> {
        if (requestedMode == ReminderMode.OFF || deadline <= now) return emptyList()
        val effectiveMode = when {
            requestedMode.requiresExactAlarm && !exactAlarmAllowed -> ReminderMode.NORMAL
            requestedMode.requiresFullScreen && !fullScreenAllowed -> ReminderMode.NORMAL
            else -> requestedMode
        }
        val firstTrigger = maxOf(deadline - leadMinutes * 60_000L, now + 1_000L)
        val count = if (effectiveMode == ReminderMode.REPEATED) REPEAT_COUNT else 1
        return List(count) { occurrence ->
            PlannedReminder(
                triggerAt = firstTrigger + occurrence * REPEAT_INTERVAL_MILLIS,
                mode = effectiveMode,
                occurrence = occurrence,
            )
        }
    }
}
