package com.yuyulife.assistant.data.repository

import com.yuyulife.assistant.data.local.LedgerDao
import com.yuyulife.assistant.data.local.LedgerEntity
import com.yuyulife.assistant.domain.model.LedgerEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LedgerRepository(private val dao: LedgerDao) {
    val entries: Flow<List<LedgerEntry>> = dao.observeAll().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun add(
        amountCents: Long,
        categoryId: Long,
        note: String,
        occurredAt: Long,
    ) {
        dao.insert(
            LedgerEntity(
                amountCents = amountCents,
                categoryId = categoryId,
                note = note.trim(),
                occurredAt = occurredAt,
            ),
        )
    }

    suspend fun delete(entry: LedgerEntry) {
        dao.deleteById(entry.id)
    }
}
