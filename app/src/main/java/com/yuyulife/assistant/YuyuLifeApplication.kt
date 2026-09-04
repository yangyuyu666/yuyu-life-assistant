package com.yuyulife.assistant

import android.app.Application
import androidx.room.Room
import com.yuyulife.assistant.data.local.AppDatabase
import com.yuyulife.assistant.data.local.DATABASE_CREATE_CALLBACK
import com.yuyulife.assistant.data.local.MIGRATION_1_2
import com.yuyulife.assistant.data.local.MIGRATION_2_3
import com.yuyulife.assistant.data.local.MIGRATION_3_4
import com.yuyulife.assistant.data.repository.LedgerRepository
import com.yuyulife.assistant.data.repository.LedgerCategoryRepository
import com.yuyulife.assistant.data.repository.SettingsRepository
import com.yuyulife.assistant.data.repository.TodoRepository
import com.yuyulife.assistant.reminder.TodoReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class YuyuLifeApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "yuyu-life.db",
        )
            .addCallback(DATABASE_CREATE_CALLBACK)
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .build()
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(applicationContext)
    }

    private val reminderScheduler: TodoReminderScheduler by lazy {
        TodoReminderScheduler(applicationContext)
    }

    val todoRepository: TodoRepository by lazy {
        TodoRepository(database.todoDao(), settingsRepository, reminderScheduler)
    }

    val ledgerRepository: LedgerRepository by lazy {
        LedgerRepository(database.ledgerDao())
    }

    val ledgerCategoryRepository: LedgerCategoryRepository by lazy {
        LedgerCategoryRepository(database.ledgerCategoryDao())
    }

    override fun onCreate() {
        super.onCreate()
        rescheduleReminders()
    }

    fun rescheduleReminders(onComplete: () -> Unit = {}) {
        applicationScope.launch {
            try {
                todoRepository.reschedulePendingReminders()
            } finally {
                onComplete()
            }
        }
    }

}
