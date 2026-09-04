package com.yuyulife.assistant.ui.ledger

import com.yuyulife.assistant.domain.model.LedgerEntry
import com.yuyulife.assistant.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test

class LedgerFiltersTest {
    @Test
    fun periodAndCategory_areAppliedTogether() {
        val entries = listOf(
            entry(id = 1, categoryId = 10, occurredAt = 150),
            entry(id = 2, categoryId = 20, occurredAt = 160),
            entry(id = 3, categoryId = 10, occurredAt = 250),
        )

        val filtered = filterLedgerEntries(entries, start = 100, endExclusive = 200, categoryId = 10)

        assertEquals(listOf(1L), filtered.map { it.id })
    }

    private fun entry(id: Long, categoryId: Long, occurredAt: Long) = LedgerEntry(
        id = id,
        type = TransactionType.EXPENSE,
        amountCents = 100,
        categoryId = categoryId,
        category = "测试",
        note = "",
        occurredAt = occurredAt,
    )
}
