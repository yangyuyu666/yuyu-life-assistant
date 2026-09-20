package com.yuyulife.assistant.ui.memo

import com.yuyulife.assistant.domain.model.MemoThreadSummary

data class MemoListUiState(
    val threads: List<MemoThreadSummary> = emptyList(),
)
