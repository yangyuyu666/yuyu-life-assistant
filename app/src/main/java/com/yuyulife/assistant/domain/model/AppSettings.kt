package com.yuyulife.assistant.domain.model

data class AppSettings(
    val todoReminderLeadMinutes: Int = DEFAULT_REMINDER_LEAD_MINUTES,
    val customBackgroundEnabled: Boolean = false,
    val customBackgroundUri: String? = null,
) {
    companion object {
        const val DEFAULT_REMINDER_LEAD_MINUTES = 30
    }
}
