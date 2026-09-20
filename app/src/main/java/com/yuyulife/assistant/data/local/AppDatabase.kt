package com.yuyulife.assistant.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        TodoEntity::class,
        LedgerEntity::class,
        LedgerCategoryEntity::class,
        MemoThreadEntity::class,
        MemoMessageEntity::class,
    ],
    version = 5,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao

    abstract fun ledgerDao(): LedgerDao

    abstract fun ledgerCategoryDao(): LedgerCategoryDao

    abstract fun memoThreadDao(): MemoThreadDao

    abstract fun memoMessageDao(): MemoMessageDao
}
