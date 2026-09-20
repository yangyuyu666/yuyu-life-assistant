package com.yuyulife.assistant.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoThreadDao {
    @Query(
        "SELECT t.id, t.title, t.createdAt, t.updatedAt, " +
            "(SELECT m.kind FROM memo_messages m WHERE m.threadId = t.id " +
            "ORDER BY m.createdAt DESC, m.id DESC LIMIT 1) AS lastMessageKind, " +
            "(SELECT m.text FROM memo_messages m WHERE m.threadId = t.id " +
            "ORDER BY m.createdAt DESC, m.id DESC LIMIT 1) AS lastText, " +
            "(SELECT m.attachmentDisplayName FROM memo_messages m WHERE m.threadId = t.id " +
            "ORDER BY m.createdAt DESC, m.id DESC LIMIT 1) AS lastAttachmentDisplayName, " +
            "(SELECT m.createdAt FROM memo_messages m WHERE m.threadId = t.id " +
            "ORDER BY m.createdAt DESC, m.id DESC LIMIT 1) AS lastMessageAt " +
            "FROM memo_threads t ORDER BY t.updatedAt DESC, t.id DESC",
    )
    fun observeSummaries(): Flow<List<MemoThreadSummaryRecord>>

    @Query("SELECT * FROM memo_threads WHERE id = :id")
    fun observeById(id: Long): Flow<MemoThreadEntity?>

    @Query("SELECT * FROM memo_threads WHERE id = :id")
    suspend fun findById(id: Long): MemoThreadEntity?

    @Insert
    suspend fun insert(thread: MemoThreadEntity): Long

    @Query("UPDATE memo_threads SET title = :title WHERE id = :id")
    suspend fun rename(id: Long, title: String): Int

    @Query("UPDATE memo_threads SET updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateActivity(id: Long, updatedAt: Long): Int

    @Query(
        "UPDATE memo_threads SET updatedAt = COALESCE(" +
            "(SELECT MAX(createdAt) FROM memo_messages WHERE threadId = :id), createdAt) " +
            "WHERE id = :id",
    )
    suspend fun recomputeActivity(id: Long)

    @Query("DELETE FROM memo_threads WHERE id = :id")
    suspend fun deleteById(id: Long): Int
}
