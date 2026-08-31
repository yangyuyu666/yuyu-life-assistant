package com.yuyulife.assistant.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.yuyulife.assistant.domain.model.TodoItem

class TodoReminderScheduler(context: Context) {
    private val appContext = context.applicationContext
    private val alarmManager = appContext.getSystemService(AlarmManager::class.java)

    fun schedule(todo: TodoItem, leadMinutes: Int) {
        val deadline = todo.deadlineAt ?: return
        val triggerAt = deadline - leadMinutes * MILLIS_PER_MINUTE
        val pendingIntent = reminderPendingIntent(todo)

        if (triggerAt <= System.currentTimeMillis() || todo.isCompleted) {
            alarmManager.cancel(pendingIntent)
            return
        }

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            pendingIntent,
        )
    }

    fun cancel(todoId: Long) {
        alarmManager.cancel(reminderPendingIntent(todoId = todoId))
    }

    private fun reminderPendingIntent(todo: TodoItem? = null, todoId: Long = todo?.id ?: 0): PendingIntent {
        val intent = Intent(appContext, TodoReminderReceiver::class.java).apply {
            action = ACTION_TODO_REMINDER
            todo?.let {
                putExtra(EXTRA_TODO_ID, it.id)
                putExtra(EXTRA_TODO_TITLE, it.title)
                putExtra(EXTRA_TODO_DEADLINE, it.deadlineAt)
            }
        }
        return PendingIntent.getBroadcast(
            appContext,
            todoId.toRequestCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun Long.toRequestCode(): Int = (this xor (this ushr 32)).toInt()

    companion object {
        const val ACTION_TODO_REMINDER = "com.yuyulife.assistant.TODO_REMINDER"
        const val EXTRA_TODO_ID = "todo_id"
        const val EXTRA_TODO_TITLE = "todo_title"
        const val EXTRA_TODO_DEADLINE = "todo_deadline"
        private const val MILLIS_PER_MINUTE = 60_000L
    }
}
