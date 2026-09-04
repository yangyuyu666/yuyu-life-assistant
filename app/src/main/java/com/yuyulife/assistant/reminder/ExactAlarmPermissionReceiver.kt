package com.yuyulife.assistant.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.yuyulife.assistant.YuyuLifeApplication

class ExactAlarmPermissionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_EXACT_ALARM_PERMISSION_CHANGED) return
        val pendingResult = goAsync()
        (context.applicationContext as YuyuLifeApplication).rescheduleReminders {
            pendingResult.finish()
        }
    }

    companion object {
        private const val ACTION_EXACT_ALARM_PERMISSION_CHANGED =
            "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED"
    }
}
