package com.yuyulife.assistant.data.local

data class LedgerCategoryUsageRecord(
    val id: Long,
    val type: String,
    val name: String,
    val sortOrder: Int,
    val usageCount: Int,
)
