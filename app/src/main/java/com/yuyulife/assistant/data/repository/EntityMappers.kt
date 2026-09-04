package com.yuyulife.assistant.data.repository

import com.yuyulife.assistant.data.local.LedgerCategoryUsageRecord
import com.yuyulife.assistant.data.local.LedgerEntryRecord
import com.yuyulife.assistant.data.local.TodoEntity
import com.yuyulife.assistant.domain.model.LedgerEntry
import com.yuyulife.assistant.domain.model.TodoItem
import com.yuyulife.assistant.domain.model.TransactionType

internal fun TodoEntity.toDomain() = TodoItem(
    id = id,
    title = title,
    createdAt = createdAt,
    deadlineAt = deadlineAt,
)

internal fun TodoItem.toEntity() = TodoEntity(
    id = id,
    title = title,
    createdAt = createdAt,
    deadlineAt = deadlineAt,
)

internal fun LedgerEntryRecord.toDomain() = LedgerEntry(
    id = id,
    type = TransactionType.entries.firstOrNull { it.name == type } ?: TransactionType.EXPENSE,
    amountCents = amountCents,
    categoryId = categoryId,
    category = category,
    note = note,
    occurredAt = occurredAt,
)

internal fun LedgerCategoryUsageRecord.toDomain() = com.yuyulife.assistant.domain.model.LedgerCategory(
    id = id,
    type = TransactionType.entries.firstOrNull { it.name == type } ?: TransactionType.EXPENSE,
    name = name,
    sortOrder = sortOrder,
    usageCount = usageCount,
)
