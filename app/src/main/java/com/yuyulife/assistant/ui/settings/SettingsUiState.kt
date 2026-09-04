package com.yuyulife.assistant.ui.settings

import com.yuyulife.assistant.domain.model.AppSettings
import com.yuyulife.assistant.domain.model.LedgerCategory

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val categories: List<LedgerCategory> = emptyList(),
)
