package com.yuyulife.assistant.domain.model

enum class ReminderMode(
    val label: String,
    val description: String,
) {
    OFF("不提醒", "不安排任何待办通知"),
    NORMAL("普通提醒", "发送一次普通通知"),
    ENHANCED("强化通知", "精确到点，使用更明显的声音和振动"),
    REPEATED("重复提醒", "每 5 分钟提醒一次，共 3 次"),
    ALARM("闹钟式提醒", "持续响铃和振动，直到关闭或稍后提醒"),
    ;

    val requiresExactAlarm: Boolean
        get() = this == ENHANCED || this == REPEATED || this == ALARM

    val requiresFullScreen: Boolean
        get() = this == ALARM
}
