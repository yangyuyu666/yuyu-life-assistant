package com.yuyulife.assistant.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoMessageDao {
    @Query(
        "SELECT * FROM memo_messages WHERE threadId = :threadId " +
            "ORDER BY createdAt ASC, id ASC",
    )
    fun observeByThreadId(threadId: Long): Flow<List<MemoMessageEntity>>

    @Query("SELECT * FROM memo_messages WHERE id = :id")
    suspend fun findById(id: Long): MemoMessageEntity?

    @Insert
    suspend fun insert(message: MemoMessageEntity): Long

    @Query("DELETE FROM memo_messages WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query(
        "SELECT attachmentPath FROM memo_messages " +
            "WHERE threadId = :threadId AND attachmentPath IS NOT NULL",
    )
    suspend fun attachmentPathsForThread(threadId: Long): List<String>

    @Query("SELECT attachmentPath FROM memo_messages WHERE attachmentPath IS NOT NULL")
    suspend fun allAttachmentPaths(): List<String>
}
