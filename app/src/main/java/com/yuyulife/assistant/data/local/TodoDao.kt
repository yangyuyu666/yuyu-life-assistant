package com.yuyulife.assistant.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Query(
        "SELECT * FROM todos " +
            "ORDER BY isCompleted ASC, deadlineAt IS NULL ASC, deadlineAt ASC, createdAt DESC",
    )
    fun observeAll(): Flow<List<TodoEntity>>

    @Insert
    suspend fun insert(item: TodoEntity): Long

    @Query("SELECT * FROM todos WHERE isCompleted = 0 AND deadlineAt IS NOT NULL")
    suspend fun getPendingWithDeadline(): List<TodoEntity>

    @Update
    suspend fun update(item: TodoEntity)

    @Delete
    suspend fun delete(item: TodoEntity)
}
