package com.yuyulife.assistant.domain.model

data class LedgerEntry(
    val id: Long,
    val type: TransactionType,
    val amountCents: Long,
    val categoryId: Long,
    val category: String,
    val note: String,
    val occurredAt: Long,
)
