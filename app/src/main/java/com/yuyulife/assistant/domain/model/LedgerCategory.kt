package com.yuyulife.assistant.domain.model

data class LedgerCategory(
    val id: Long,
    val type: TransactionType,
    val name: String,
    val sortOrder: Int,
    val usageCount: Int = 0,
)
