package com.yuyulife.assistant.ui.ledger

import com.yuyulife.assistant.domain.model.LedgerEntry

internal fun filterLedgerEntries(
    entries: List<LedgerEntry>,
    start: Long,
    endExclusive: Long,
    categoryId: Long?,
): List<LedgerEntry> = entries.filter { entry ->
    entry.occurredAt in start until endExclusive &&
        (categoryId == null || entry.categoryId == categoryId)
}
