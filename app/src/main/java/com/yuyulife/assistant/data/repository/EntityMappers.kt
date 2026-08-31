package com.yuyulife.assistant.data.repository

import com.yuyulife.assistant.data.local.LedgerEntity
import com.yuyulife.assistant.data.local.TodoEntity
import com.yuyulife.assistant.domain.model.LedgerEntry
import com.yuyulife.assistant.domain.model.TodoItem
import com.yuyulife.assistant.domain.model.TransactionType

internal fun TodoEntity.toDomain() = TodoItem(
    id = id,
    title = title,
    isCompleted = isCompleted,
    createdAt = createdAt,
)

internal fun TodoItem.toEntity() = TodoEntity(
    id = id,
    title = title,
    isCompleted = isCompleted,
    createdAt = createdAt,
)

internal fun LedgerEntity.toDomain() = LedgerEntry(
    id = id,
    type = TransactionType.entries.firstOrNull { it.name == type } ?: TransactionType.EXPENSE,
    amountCents = amountCents,
    category = category,
    note = note,
    occurredAt = occurredAt,
)

internal fun LedgerEntry.toEntity() = LedgerEntity(
    id = id,
    type = type.name,
    amountCents = amountCents,
    category = category,
    note = note,
    occurredAt = occurredAt,
)

