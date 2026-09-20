package com.yuyulife.assistant.ui.home

enum class AppSection(
    val label: String,
    val symbol: String,
) {
    TODO(label = "待办", symbol = "✓"),
    LEDGER(label = "记账", symbol = "¥"),
    MEMO(label = "备忘录", symbol = "记"),
    SETTINGS(label = "设置", symbol = "⚙"),
}
