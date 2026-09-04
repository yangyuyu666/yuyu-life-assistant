package com.yuyulife.assistant.reminder

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

class ReminderPermissionManager(context: Context) {
    private val appContext = context.applicationContext

    fun canScheduleExactAlarms(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return appContext.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
    }

    fun canUseFullScreenIntent(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return true
        return appContext.getSystemService(NotificationManager::class.java).canUseFullScreenIntent()
    }

    fun exactAlarmSettingsIntent(): Intent = Intent(
        Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
        Uri.parse("package:${appContext.packageName}"),
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    fun fullScreenSettingsIntent(): Intent = Intent(
        Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
        Uri.parse("package:${appContext.packageName}"),
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}
