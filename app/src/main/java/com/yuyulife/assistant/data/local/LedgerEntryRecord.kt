package com.yuyulife.assistant.data.local

data class LedgerEntryRecord(
    val id: Long,
    val type: String,
    val amountCents: Long,
    val categoryId: Long,
    val category: String,
    val note: String,
    val occurredAt: Long,
)
