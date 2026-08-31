package com.yuyulife.assistant.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.yuyulife.assistant.YuyuLifeApplication

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            (context.applicationContext as YuyuLifeApplication).rescheduleReminders {
                pendingResult.finish()
            }
        }
    }
}
