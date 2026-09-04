package com.yuyulife.assistant.ui.ledger

import com.yuyulife.assistant.domain.model.LedgerEntry
import com.yuyulife.assistant.domain.model.LedgerCategory
import com.yuyulife.assistant.domain.model.LedgerSummary

data class LedgerUiState(
    val entries: List<LedgerEntry> = emptyList(),
    val summary: LedgerSummary = LedgerSummary(),
    val periodMode: LedgerPeriodMode = LedgerPeriodMode.DAY,
    val selectedPeriod: Long = System.currentTimeMillis(),
    val categories: List<LedgerCategory> = emptyList(),
    val selectedCategoryId: Long? = null,
)
