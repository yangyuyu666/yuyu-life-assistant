package com.yuyulife.assistant.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class LedgerSummaryTest {
    @Test
    fun `summary separates income and expense`() {
        val entries = listOf(
            entry(TransactionType.INCOME, 100_000),
            entry(TransactionType.EXPENSE, 2_500),
            entry(TransactionType.EXPENSE, 1_500),
        )

        val summary = LedgerSummary.from(entries)

        assertEquals(100_000L, summary.incomeCents)
        assertEquals(4_000L, summary.expenseCents)
        assertEquals(96_000L, summary.balanceCents)
    }

    private fun entry(type: TransactionType, amountCents: Long) = LedgerEntry(
        id = 0,
        type = type,
        amountCents = amountCents,
        category = "测试",
        note = "",
        occurredAt = 0,
    )
}
