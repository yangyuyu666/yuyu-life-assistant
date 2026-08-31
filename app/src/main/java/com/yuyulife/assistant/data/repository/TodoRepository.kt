package com.yuyulife.assistant.data.repository

import com.yuyulife.assistant.data.local.TodoDao
import com.yuyulife.assistant.data.local.TodoEntity
import com.yuyulife.assistant.domain.model.TodoItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TodoRepository(private val dao: TodoDao) {
    val items: Flow<List<TodoItem>> = dao.observeAll().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun add(title: String) {
        dao.insert(
            TodoEntity(
                title = title.trim(),
                createdAt = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun setCompleted(item: TodoItem, completed: Boolean) {
        dao.update(item.copy(isCompleted = completed).toEntity())
    }

    suspend fun delete(item: TodoItem) {
        dao.delete(item.toEntity())
    }
}
