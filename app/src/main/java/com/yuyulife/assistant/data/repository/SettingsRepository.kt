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

    fun setCustomBackgroundEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_CUSTOM_BACKGROUND_ENABLED, enabled).apply()
        mutableSettings.value = mutableSettings.value.copy(
            customBackgroundEnabled = enabled,
        )
    }

    fun setCustomBackground(uri: String) {
        preferences.edit()
            .putString(KEY_CUSTOM_BACKGROUND_URI, uri)
            .putBoolean(KEY_CUSTOM_BACKGROUND_ENABLED, true)
            .apply()
        mutableSettings.value = mutableSettings.value.copy(
            customBackgroundEnabled = true,
            customBackgroundUri = uri,
        )
    }

    private fun readSettings() = AppSettings(
        todoReminderLeadMinutes = preferences.getInt(
            KEY_TODO_REMINDER_LEAD_MINUTES,
            AppSettings.DEFAULT_REMINDER_LEAD_MINUTES,
        ),
        customBackgroundEnabled = preferences.getBoolean(
            KEY_CUSTOM_BACKGROUND_ENABLED,
            false,
        ),
        customBackgroundUri = preferences.getString(KEY_CUSTOM_BACKGROUND_URI, null),
    )

    companion object {
        private const val PREFERENCES_NAME = "yuyu_life_settings"
        private const val KEY_TODO_REMINDER_LEAD_MINUTES = "todo_reminder_lead_minutes"
        private const val KEY_CUSTOM_BACKGROUND_ENABLED = "custom_background_enabled"
        private const val KEY_CUSTOM_BACKGROUND_URI = "custom_background_uri"
    }
}
