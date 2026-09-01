package com.yuyulife.assistant.data.repository

import com.yuyulife.assistant.data.local.TodoDao
import com.yuyulife.assistant.data.local.TodoEntity
import com.yuyulife.assistant.domain.model.TodoItem
import com.yuyulife.assistant.reminder.TodoReminderScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TodoRepository(
    private val dao: TodoDao,
    private val settingsRepository: SettingsRepository,
    private val reminderScheduler: TodoReminderScheduler,
) {
    val items: Flow<List<TodoItem>> = dao.observeAll().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun add(title: String, deadlineAt: Long) {
        val entity = TodoEntity(
            title = title.trim(),
            createdAt = System.currentTimeMillis(),
            deadlineAt = deadlineAt,
        )
        val id = dao.insert(entity)
        scheduleReminder(entity.copy(id = id).toDomain())
    }

    suspend fun delete(item: TodoItem) {
        dao.delete(item.toEntity())
        reminderScheduler.cancel(item.id)
    }

    suspend fun delete(items: List<TodoItem>) {
        if (items.isEmpty()) return
        dao.delete(items.map { it.toEntity() })
        items.forEach { reminderScheduler.cancel(it.id) }
    }

    suspend fun reschedulePendingReminders() {
        dao.getPendingWithDeadline()
            .map { it.toDomain() }
            .forEach(::scheduleReminder)
    }

    private fun scheduleReminder(item: TodoItem) {
        reminderScheduler.schedule(
            todo = item,
            leadMinutes = settingsRepository.settings.value.todoReminderLeadMinutes,
        )
    }
}
