package com.yuyulife.assistant.data.repository

import android.content.Context
import com.yuyulife.assistant.domain.model.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {
    private val preferences = context.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE,
    )
    private val mutableSettings = MutableStateFlow(readSettings())

    val settings: StateFlow<AppSettings> = mutableSettings.asStateFlow()

    fun setTodoReminderLeadMinutes(minutes: Int) {
        require(minutes >= 0) { "Reminder lead time cannot be negative" }
        preferences.edit().putInt(KEY_TODO_REMINDER_LEAD_MINUTES, minutes).apply()
        mutableSettings.value = mutableSettings.value.copy(
            todoReminderLeadMinutes = minutes,
        )
    }

    private fun readSettings() = AppSettings(
        todoReminderLeadMinutes = preferences.getInt(
            KEY_TODO_REMINDER_LEAD_MINUTES,
            AppSettings.DEFAULT_REMINDER_LEAD_MINUTES,
        ),
    )

    companion object {
        private const val PREFERENCES_NAME = "yuyu_life_settings"
        private const val KEY_TODO_REMINDER_LEAD_MINUTES = "todo_reminder_lead_minutes"
    }
}
