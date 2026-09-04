package com.yuyulife.assistant.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ledger_categories",
    indices = [Index(value = ["type", "name"], unique = true)],
)
data class LedgerCategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,
    val name: String,
    val sortOrder: Int,
)
