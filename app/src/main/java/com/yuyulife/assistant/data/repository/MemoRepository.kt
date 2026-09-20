package com.yuyulife.assistant.data.repository

import android.net.Uri
import androidx.room.withTransaction
import com.yuyulife.assistant.data.local.AppDatabase
import com.yuyulife.assistant.data.local.MemoMessageEntity
import com.yuyulife.assistant.data.local.MemoThreadEntity
import com.yuyulife.assistant.data.storage.MemoAttachmentStore
import com.yuyulife.assistant.domain.model.MemoMessage
import com.yuyulife.assistant.domain.model.MemoMessageKind
import com.yuyulife.assistant.domain.model.MemoThread
import com.yuyulife.assistant.domain.model.MemoThreadSummary
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class MemoImportResult(
    val succeeded: Int,
    val failed: Int,
)

class MemoRepository(
    private val database: AppDatabase,
    private val attachmentStore: MemoAttachmentStore,
) {
    private val threadDao = database.memoThreadDao()
    private val messageDao = database.memoMessageDao()
    private val fileMutex = Mutex()

    val threads: Flow<List<MemoThreadSummary>> = threadDao.observeSummaries().map { records ->
        records.map { record ->
            MemoThreadSummary(
                thread = MemoThread(record.id, record.title, record.createdAt, record.updatedAt),
                lastMessageKind = record.lastMessageKind?.toMemoMessageKindOrNull(),
                lastText = record.lastText,
                lastAttachmentDisplayName = record.lastAttachmentDisplayName,
                lastMessageAt = record.lastMessageAt,
            )
        }
    }

    fun observeThread(id: Long): Flow<MemoThread?> = threadDao.observeById(id).map { entity ->
        entity?.let { MemoThread(it.id, it.title, it.createdAt, it.updatedAt) }
    }

    fun observeMessages(threadId: Long): Flow<List<MemoMessage>> =
        messageDao.observeByThreadId(threadId).map { entities -> entities.map { it.toDomain() } }

    suspend fun createThread(title: String): Long {
        val normalized = requireTitle(title)
        val now = System.currentTimeMillis()
        return threadDao.insert(MemoThreadEntity(title = normalized, createdAt = now, updatedAt = now))
    }

    suspend fun renameThread(id: Long, title: String) {
        threadDao.rename(id, requireTitle(title))
    }

    suspend fun sendText(threadId: Long, text: String) {
        val normalized = text.trim()
        if (normalized.isBlank() || threadDao.findById(threadId) == null) return
        val now = System.currentTimeMillis()
        database.withTransaction {
            messageDao.insert(
                MemoMessageEntity(
                    threadId = threadId,
                    kind = MemoMessageKind.TEXT.name,
                    text = normalized,
                    attachmentPath = null,
                    attachmentDisplayName = null,
                    attachmentMimeType = null,
                    attachmentSizeBytes = null,
                    createdAt = now,
                ),
            )
            threadDao.updateActivity(threadId, now)
        }
    }

    suspend fun importFiles(threadId: Long, uris: List<Uri>): MemoImportResult =
        withContext(Dispatchers.IO) {
            fileMutex.withLock {
                if (threadDao.findById(threadId) == null) {
                    return@withLock MemoImportResult(succeeded = 0, failed = uris.size)
                }
                var succeeded = 0
                var failed = 0
                uris.forEachIndexed { index, uri ->
                    val attachment = runCatching { attachmentStore.import(uri, threadId) }.getOrElse {
                        failed += 1
                        return@forEachIndexed
                    }
                    val createdAt = System.currentTimeMillis() + index
                    try {
                        database.withTransaction {
                            messageDao.insert(
                                MemoMessageEntity(
                                    threadId = threadId,
                                    kind = MemoMessageKind.FILE.name,
                                    text = null,
                                    attachmentPath = attachment.relativePath,
                                    attachmentDisplayName = attachment.displayName,
                                    attachmentMimeType = attachment.mimeType,
                                    attachmentSizeBytes = attachment.sizeBytes,
                                    createdAt = createdAt,
                                ),
                            )
                            threadDao.updateActivity(threadId, createdAt)
                        }
                        succeeded += 1
                    } catch (_: Exception) {
                        attachmentStore.delete(attachment.relativePath)
                        failed += 1
                    }
                }
                MemoImportResult(succeeded = succeeded, failed = failed)
            }
        }

    suspend fun deleteMessage(messageId: Long) = fileMutex.withLock {
        val message = messageDao.findById(messageId) ?: return@withLock
        database.withTransaction {
            messageDao.deleteById(messageId)
            threadDao.recomputeActivity(message.threadId)
        }
        attachmentStore.delete(message.attachmentPath)
    }

    suspend fun deleteThread(threadId: Long) = fileMutex.withLock {
        val attachmentPaths = messageDao.attachmentPathsForThread(threadId)
        database.withTransaction { threadDao.deleteById(threadId) }
        attachmentPaths.forEach(attachmentStore::delete)
        attachmentStore.deleteThreadDirectory(threadId)
    }

    suspend fun cleanOrphanedAttachments() = withContext(Dispatchers.IO) {
        fileMutex.withLock {
            attachmentStore.cleanOrphans(messageDao.allAttachmentPaths().toSet())
        }
    }

    fun resolveAttachment(relativePath: String?): File? = attachmentStore.resolve(relativePath)

    fun attachmentContentUri(relativePath: String?): Uri? = attachmentStore.contentUri(relativePath)

    private fun requireTitle(title: String): String {
        val normalized = title.trim()
        require(normalized.isNotBlank()) { "备忘录名称不能为空" }
        return normalized
    }
}

private fun MemoMessageEntity.toDomain() = MemoMessage(
    id = id,
    threadId = threadId,
    kind = kind.toMemoMessageKindOrNull() ?: MemoMessageKind.FILE,
    text = text,
    attachmentPath = attachmentPath,
    attachmentDisplayName = attachmentDisplayName,
    attachmentMimeType = attachmentMimeType,
    attachmentSizeBytes = attachmentSizeBytes,
    createdAt = createdAt,
)

private fun String.toMemoMessageKindOrNull(): MemoMessageKind? =
    runCatching { MemoMessageKind.valueOf(this) }.getOrNull()
