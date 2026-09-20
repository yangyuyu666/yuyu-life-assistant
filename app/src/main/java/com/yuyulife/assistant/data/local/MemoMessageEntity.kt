package com.yuyulife.assistant.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "memo_messages",
    foreignKeys = [
        ForeignKey(
            entity = MemoThreadEntity::class,
            parentColumns = ["id"],
            childColumns = ["threadId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("threadId"), Index(value = ["threadId", "createdAt"])],
)
data class MemoMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val threadId: Long,
    val kind: String,
    val text: String?,
    val attachmentPath: String?,
    val attachmentDisplayName: String?,
    val attachmentMimeType: String?,
    val attachmentSizeBytes: Long?,
    val createdAt: Long,
)
