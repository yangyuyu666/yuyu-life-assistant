package com.yuyulife.assistant.ui.memo

import com.yuyulife.assistant.domain.model.MemoMessage
import com.yuyulife.assistant.domain.model.MemoThread

data class MemoChatUiState(
    val thread: MemoThread? = null,
    val messages: List<MemoMessage> = emptyList(),
    val isLoaded: Boolean = false,
)
