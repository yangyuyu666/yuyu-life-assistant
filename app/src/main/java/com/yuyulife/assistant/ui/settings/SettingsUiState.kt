package com.yuyulife.assistant.ui.settings

import com.yuyulife.assistant.domain.model.AppSettings

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
)
