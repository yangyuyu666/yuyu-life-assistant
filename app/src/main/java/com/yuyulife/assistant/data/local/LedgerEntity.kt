package com.yuyulife.assistant.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ledger_entries")
data class LedgerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,
    val amountCents: Long,
    val category: String,
    val note: String,
    val occurredAt: Long,
)

