package com.yuyulife.assistant.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LedgerDao {
    @Query(
        "SELECT e.id, c.type, e.amountCents, e.categoryId, c.name AS category, e.note, e.occurredAt " +
            "FROM ledger_entries e " +
            "INNER JOIN ledger_categories c ON c.id = e.categoryId " +
            "ORDER BY e.occurredAt DESC, e.id DESC",
    )
    fun observeAll(): Flow<List<LedgerEntryRecord>>

    @Insert
    suspend fun insert(entry: LedgerEntity)

    @Query("DELETE FROM ledger_entries WHERE id = :id")
    suspend fun deleteById(id: Long)
}
