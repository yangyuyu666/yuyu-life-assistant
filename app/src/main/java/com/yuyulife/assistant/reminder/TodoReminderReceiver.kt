package com.yuyulife.assistant.reminder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import com.yuyulife.assistant.MainActivity
import com.yuyulife.assistant.R
import com.yuyulife.assistant.domain.model.ReminderMode
import com.yuyulife.assistant.util.formatDateTime

class TodoReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) return

        val todoId = intent.getLongExtra(TodoReminderScheduler.EXTRA_TODO_ID, 0)
        val title = intent.getStringExtra(TodoReminderScheduler.EXTRA_TODO_TITLE)
            ?: context.getString(R.string.todo_reminder_default_title)
        val deadline = intent.getLongExtra(TodoReminderScheduler.EXTRA_TODO_DEADLINE, 0)
        val mode = intent.getStringExtra(TodoReminderScheduler.EXTRA_REMINDER_MODE)
            ?.let { saved -> ReminderMode.entries.firstOrNull { it.name == saved } }
            ?: ReminderMode.NORMAL
        val manager = context.getSystemService(NotificationManager::class.java)
        createChannels(context, manager)

        val openIntent = if (mode == ReminderMode.ALARM) {
            alarmActivityIntent(context, todoId, title, deadline)
        } else {
            PendingIntent.getActivity(
                context,
                TodoReminderScheduler.notificationId(todoId),
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        val deadlineText = if (deadline > 0) formatDateTime(deadline) else ""
        val channelId = when (mode) {
            ReminderMode.NORMAL, ReminderMode.OFF -> NORMAL_CHANNEL_ID
            ReminderMode.ENHANCED, ReminderMode.REPEATED -> STRONG_CHANNEL_ID
            ReminderMode.ALARM -> ALARM_CHANNEL_ID
        }
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, channelId)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(context)
                .setPriority(if (mode == ReminderMode.NORMAL) Notification.PRIORITY_HIGH else Notification.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL)
        }

        builder
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(if (mode == ReminderMode.ALARM) "待办闹钟" else context.getString(R.string.todo_reminder_title))
            .setContentText("$title · 截止 $deadlineText")
            .setStyle(Notification.BigTextStyle().bigText("$title\n截止时间：$deadlineText"))
            .setContentIntent(openIntent)
            .setAutoCancel(mode != ReminderMode.ALARM)
            .setOngoing(mode == ReminderMode.ALARM)
            .setCategory(if (mode == ReminderMode.ALARM) Notification.CATEGORY_ALARM else Notification.CATEGORY_REMINDER)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(false)

        if (mode == ReminderMode.ALARM) {
            builder.setFullScreenIntent(openIntent, true)
        }

        manager.notify(TodoReminderScheduler.notificationId(todoId), builder.build())
    }

    private fun alarmActivityIntent(
        context: Context,
        todoId: Long,
        title: String,
        deadline: Long,
    ): PendingIntent {
        val intent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(TodoReminderScheduler.EXTRA_TODO_ID, todoId)
            putExtra(TodoReminderScheduler.EXTRA_TODO_TITLE, title)
            putExtra(TodoReminderScheduler.EXTRA_TODO_DEADLINE, deadline)
        }
        return PendingIntent.getActivity(
            context,
            TodoReminderScheduler.notificationId(todoId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun createChannels(context: Context, manager: NotificationManager) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        manager.createNotificationChannel(
            NotificationChannel(
                NORMAL_CHANNEL_ID,
                context.getString(R.string.todo_reminder_channel_name),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = context.getString(R.string.todo_reminder_channel_description)
            },
        )

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val alarmAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .build()
        manager.createNotificationChannel(
            NotificationChannel(
                STRONG_CHANNEL_ID,
                context.getString(R.string.todo_strong_reminder_channel_name),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = context.getString(R.string.todo_strong_reminder_channel_description)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 700, 300, 700)
                setSound(alarmSound, alarmAttributes)
            },
        )
        manager.createNotificationChannel(
            NotificationChannel(
                ALARM_CHANNEL_ID,
                context.getString(R.string.todo_alarm_channel_name),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = context.getString(R.string.todo_alarm_channel_description)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 900, 300, 900)
                setSound(alarmSound, alarmAttributes)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            },
        )
    }

    companion object {
        private const val NORMAL_CHANNEL_ID = "todo_deadline_reminders"
        private const val STRONG_CHANNEL_ID = "todo_strong_reminders_v1"
        private const val ALARM_CHANNEL_ID = "todo_alarm_reminders_v1"
    }
}
