package com.yuyulife.assistant

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.yuyulife.assistant.data.local.AppDatabase
import com.yuyulife.assistant.data.repository.LedgerRepository
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
        ).addMigrations(MIGRATION_1_2).build()
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

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE todos ADD COLUMN deadlineAt INTEGER")
            }
        }
    }
}
