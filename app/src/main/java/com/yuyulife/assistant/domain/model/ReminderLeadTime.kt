package com.yuyulife.assistant.domain.model

data class ReminderLeadTime(
    val minutes: Int,
    val label: String,
) {
    companion object {
        val options = listOf(
            ReminderLeadTime(0, "到截止时间提醒"),
            ReminderLeadTime(5, "提前 5 分钟"),
            ReminderLeadTime(15, "提前 15 分钟"),
            ReminderLeadTime(30, "提前 30 分钟"),
            ReminderLeadTime(60, "提前 1 小时"),
            ReminderLeadTime(180, "提前 3 小时"),
            ReminderLeadTime(1_440, "提前 1 天"),
        )

        fun labelFor(minutes: Int): String = options
            .firstOrNull { it.minutes == minutes }
            ?.label
            ?: "提前 $minutes 分钟"
    }
}
