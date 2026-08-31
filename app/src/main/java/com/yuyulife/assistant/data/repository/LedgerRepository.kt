package com.yuyulife.assistant.data.repository

import com.yuyulife.assistant.data.local.LedgerDao
import com.yuyulife.assistant.data.local.LedgerEntity
import com.yuyulife.assistant.domain.model.LedgerEntry
import com.yuyulife.assistant.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LedgerRepository(private val dao: LedgerDao) {
    val entries: Flow<List<LedgerEntry>> = dao.observeAll().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun add(
        type: TransactionType,
        amountCents: Long,
        category: String,
        note: String,
        occurredAt: Long,
    ) {
        dao.insert(
            LedgerEntity(
                type = type.name,
                amountCents = amountCents,
                category = category,
                note = note.trim(),
                occurredAt = occurredAt,
            ),
        )
    }

    suspend fun delete(entry: LedgerEntry) {
        dao.delete(entry.toEntity())
    }
}
