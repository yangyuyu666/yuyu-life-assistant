package com.yuyulife.assistant.domain.model

data class AppSettings(
    val todoReminderLeadMinutes: Int = DEFAULT_REMINDER_LEAD_MINUTES,
) {
    companion object {
        const val DEFAULT_REMINDER_LEAD_MINUTES = 30
    }
}
