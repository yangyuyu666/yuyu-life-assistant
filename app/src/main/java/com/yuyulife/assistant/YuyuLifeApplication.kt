package com.yuyulife.assistant

import android.app.Application
import androidx.room.Room
import com.yuyulife.assistant.data.local.AppDatabase
import com.yuyulife.assistant.data.repository.LedgerRepository
import com.yuyulife.assistant.data.repository.TodoRepository

class YuyuLifeApplication : Application() {
    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "yuyu-life.db",
        ).build()
    }

    val todoRepository: TodoRepository by lazy {
        TodoRepository(database.todoDao())
    }

    val ledgerRepository: LedgerRepository by lazy {
        LedgerRepository(database.ledgerDao())
    }
}

