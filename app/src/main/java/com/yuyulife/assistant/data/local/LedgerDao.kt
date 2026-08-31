package com.yuyulife.assistant.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LedgerDao {
    @Query("SELECT * FROM ledger_entries ORDER BY occurredAt DESC, id DESC")
    fun observeAll(): Flow<List<LedgerEntity>>

    @Insert
    suspend fun insert(entry: LedgerEntity)

    @Delete
    suspend fun delete(entry: LedgerEntity)
}

