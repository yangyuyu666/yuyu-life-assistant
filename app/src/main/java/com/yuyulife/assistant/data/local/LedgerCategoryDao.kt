package com.yuyulife.assistant.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface LedgerCategoryDao {
    @Query(
        "SELECT c.id, c.type, c.name, c.sortOrder, COUNT(e.id) AS usageCount " +
            "FROM ledger_categories c " +
            "LEFT JOIN ledger_entries e ON e.categoryId = c.id " +
            "GROUP BY c.id " +
            "ORDER BY c.type, c.sortOrder, c.id",
    )
    fun observeAllWithUsage(): Flow<List<LedgerCategoryUsageRecord>>

    @Query("SELECT * FROM ledger_categories WHERE id = :id")
    suspend fun findById(id: Long): LedgerCategoryEntity?

    @Query("SELECT COUNT(*) FROM ledger_categories WHERE type = :type AND name = :name AND id != :excludeId")
    suspend fun countByName(type: String, name: String, excludeId: Long = 0): Int

    @Query("SELECT COALESCE(MAX(sortOrder), -1) + 1 FROM ledger_categories WHERE type = :type")
    suspend fun nextSortOrder(type: String): Int

    @Query("SELECT COUNT(*) FROM ledger_entries WHERE categoryId = :categoryId")
    suspend fun usageCount(categoryId: Long): Int

    @Insert
    suspend fun insert(category: LedgerCategoryEntity): Long

    @Query("UPDATE ledger_categories SET name = :name WHERE id = :id")
    suspend fun rename(id: Long, name: String): Int

    @Query("DELETE FROM ledger_categories WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Transaction
    suspend fun deleteIfUnused(id: Long): Boolean {
        if (usageCount(id) > 0) return false
        return deleteById(id) > 0
    }
}
