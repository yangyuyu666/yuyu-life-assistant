package com.yuyulife.assistant.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.yuyulife.assistant.MainActivity
import com.yuyulife.assistant.R
import com.yuyulife.assistant.util.formatDateTime

class TodoReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val todoId = intent.getLongExtra(TodoReminderScheduler.EXTRA_TODO_ID, 0)
        val title = intent.getStringExtra(TodoReminderScheduler.EXTRA_TODO_TITLE)
            ?: context.getString(R.string.todo_reminder_default_title)
        val deadline = intent.getLongExtra(TodoReminderScheduler.EXTRA_TODO_DEADLINE, 0)
        val manager = context.getSystemService(NotificationManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.todo_reminder_channel_name),
                    NotificationManager.IMPORTANCE_HIGH,
                ).apply {
                    description = context.getString(R.string.todo_reminder_channel_description)
                },
            )
        }

        val openAppIntent = PendingIntent.getActivity(
            context,
            todoId.toRequestCode(),
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val deadlineText = if (deadline > 0) formatDateTime(deadline) else ""
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            android.app.Notification.Builder(context, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            android.app.Notification.Builder(context)
        }
        val notification = builder
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.todo_reminder_title))
            .setContentText("$title · 截止 $deadlineText")
            .setStyle(android.app.Notification.BigTextStyle().bigText("$title\n截止时间：$deadlineText"))
            .setContentIntent(openAppIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(todoId.toRequestCode(), notification)
    }

    private fun Long.toRequestCode(): Int = (this xor (this ushr 32)).toInt()

    companion object {
        private const val CHANNEL_ID = "todo_deadline_reminders"
    }
}
