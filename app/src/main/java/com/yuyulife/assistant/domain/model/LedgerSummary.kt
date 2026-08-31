package com.yuyulife.assistant.domain.model

data class LedgerSummary(
    val incomeCents: Long = 0,
    val expenseCents: Long = 0,
) {
    val balanceCents: Long
        get() = incomeCents - expenseCents

    companion object {
        fun from(entries: List<LedgerEntry>): LedgerSummary = LedgerSummary(
            incomeCents = entries
                .filter { it.type == TransactionType.INCOME }
                .sumOf { it.amountCents },
            expenseCents = entries
                .filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amountCents },
        )
    }
}

