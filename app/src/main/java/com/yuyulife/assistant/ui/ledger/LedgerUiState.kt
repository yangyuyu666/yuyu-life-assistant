package com.yuyulife.assistant.ui.ledger

import com.yuyulife.assistant.domain.model.LedgerEntry
import com.yuyulife.assistant.domain.model.LedgerSummary

data class LedgerUiState(
    val entries: List<LedgerEntry> = emptyList(),
    val summary: LedgerSummary = LedgerSummary(),
)

