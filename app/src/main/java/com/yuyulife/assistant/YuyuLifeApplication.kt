package com.yuyulife.assistant

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.yuyulife.assistant.data.local.AppDatabase
import com.yuyulife.assistant.data.repository.LedgerRepository
import com.yuyulife.assistant.data.repository.TodoRepository

class YuyuLifeApplication : Application() {
    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "yuyu-life.db",
        ).addMigrations(MIGRATION_1_2).build()
    }

    val todoRepository: TodoRepository by lazy {
        TodoRepository(database.todoDao())
    }

    val ledgerRepository: LedgerRepository by lazy {
        LedgerRepository(database.ledgerDao())
    }

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE todos ADD COLUMN deadlineAt INTEGER")
            }
        }
    }
}
