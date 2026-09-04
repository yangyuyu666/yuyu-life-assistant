package com.yuyulife.assistant.reminder

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.yuyulife.assistant.domain.model.ReminderMode
import com.yuyulife.assistant.domain.model.TodoItem

class TodoReminderScheduler(context: Context) {
    private val appContext = context.applicationContext
    private val alarmManager = appContext.getSystemService(AlarmManager::class.java)
    private val notificationManager = appContext.getSystemService(NotificationManager::class.java)
    private val permissionManager = ReminderPermissionManager(appContext)

    fun schedule(todo: TodoItem, leadMinutes: Int, mode: ReminderMode) {
        cancel(todo.id)
        val deadline = todo.deadlineAt ?: return
        ReminderSchedulePlanner.plan(
            deadline = deadline,
            now = System.currentTimeMillis(),
            leadMinutes = leadMinutes,
            requestedMode = mode,
            exactAlarmAllowed = permissionManager.canScheduleExactAlarms(),
            fullScreenAllowed = permissionManager.canUseFullScreenIntent(),
        ).forEach { reminder ->
            scheduleAt(
                todo = todo,
                triggerAt = reminder.triggerAt,
                mode = reminder.mode,
                occurrence = reminder.occurrence,
            )
        }
    }

    fun snooze(todo: TodoItem) {
        cancel(todo.id)
        val mode = effectiveMode(ReminderMode.ALARM)
        scheduleAt(
            todo = todo,
            triggerAt = System.currentTimeMillis() + SNOOZE_MILLIS,
            mode = mode,
            occurrence = SNOOZE_OCCURRENCE,
        )
    }

    fun cancel(todoId: Long) {
        (0 until ReminderSchedulePlanner.REPEAT_COUNT).forEach { occurrence ->
            alarmManager.cancel(reminderPendingIntent(todoId, occurrence))
        }
        alarmManager.cancel(reminderPendingIntent(todoId, SNOOZE_OCCURRENCE))
        notificationManager.cancel(notificationId(todoId))
    }

    private fun effectiveMode(requested: ReminderMode): ReminderMode {
        if (requested.requiresExactAlarm && !permissionManager.canScheduleExactAlarms()) {
            return ReminderMode.NORMAL
        }
        if (requested.requiresFullScreen && !permissionManager.canUseFullScreenIntent()) {
            return ReminderMode.NORMAL
        }
        return requested
    }

    private fun scheduleAt(
        todo: TodoItem,
        triggerAt: Long,
        mode: ReminderMode,
        occurrence: Int,
    ) {
        val pendingIntent = reminderPendingIntent(todo.id, occurrence, todo, mode)
        if (mode.requiresExactAlarm && permissionManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                pendingIntent,
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                pendingIntent,
            )
        }
    }

    private fun reminderPendingIntent(
        todoId: Long,
        occurrence: Int,
        todo: TodoItem? = null,
        mode: ReminderMode = ReminderMode.NORMAL,
    ): PendingIntent {
        val intent = Intent(appContext, TodoReminderReceiver::class.java).apply {
            action = ACTION_TODO_REMINDER
            data = Uri.parse("yuyu://todo-reminder/$todoId/$occurrence")
            todo?.let {
                putExtra(EXTRA_TODO_ID, it.id)
                putExtra(EXTRA_TODO_TITLE, it.title)
                putExtra(EXTRA_TODO_DEADLINE, it.deadlineAt)
                putExtra(EXTRA_REMINDER_MODE, mode.name)
            }
        }
        return PendingIntent.getBroadcast(
            appContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        const val ACTION_TODO_REMINDER = "com.yuyulife.assistant.TODO_REMINDER"
        const val EXTRA_TODO_ID = "todo_id"
        const val EXTRA_TODO_TITLE = "todo_title"
        const val EXTRA_TODO_DEADLINE = "todo_deadline"
        const val EXTRA_REMINDER_MODE = "reminder_mode"
        const val SNOOZE_MINUTES = 5
        private const val SNOOZE_OCCURRENCE = 99
        private const val MILLIS_PER_MINUTE = 60_000L
        private const val SNOOZE_MILLIS = SNOOZE_MINUTES * MILLIS_PER_MINUTE

        fun notificationId(todoId: Long): Int = (todoId xor (todoId ushr 32)).toInt()
    }
}
