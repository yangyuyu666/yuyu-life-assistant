package com.yuyulife.assistant.data.storage

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.util.UUID

data class ImportedMemoAttachment(
    val relativePath: String,
    val displayName: String,
    val mimeType: String,
    val sizeBytes: Long,
)

class MemoAttachmentStore(private val context: Context) {
    private val root = File(context.filesDir, ROOT_DIRECTORY)

    fun import(uri: Uri, threadId: Long): ImportedMemoAttachment {
        val metadata = readMetadata(uri)
        root.mkdirs()
        if (metadata.sizeBytes != null && metadata.sizeBytes > root.usableSpace) {
            throw IOException("手机剩余空间不足")
        }

        val threadDirectory = File(root, threadId.toString()).apply { mkdirs() }
        val extension = metadata.displayName.substringAfterLast('.', "")
            .takeIf { it.length in 1..12 && it.all(Char::isLetterOrDigit) }
            ?.let { ".$it" }
            .orEmpty()
        val baseName = UUID.randomUUID().toString()
        val temporaryFile = File(threadDirectory, "$baseName.part")
        val destinationFile = File(threadDirectory, "$baseName$extension")

        try {
            val input = context.contentResolver.openInputStream(uri)
                ?: throw IOException("无法读取所选文件")
            val copiedBytes = input.use { source ->
                temporaryFile.outputStream().buffered().use { destination ->
                    source.copyTo(destination)
                }
            }
            if (!temporaryFile.renameTo(destinationFile)) {
                throw IOException("无法保存所选文件")
            }
            return ImportedMemoAttachment(
                relativePath = destinationFile.relativeTo(context.filesDir).invariantSeparatorsPath,
                displayName = metadata.displayName,
                mimeType = metadata.mimeType,
                sizeBytes = copiedBytes,
            )
        } catch (error: Exception) {
            temporaryFile.delete()
            destinationFile.delete()
            throw error
        }
    }

    fun resolve(relativePath: String?): File? {
        if (relativePath.isNullOrBlank()) return null
        return runCatching {
            val candidate = File(context.filesDir, relativePath).canonicalFile
            val canonicalRoot = root.canonicalFile
            val insideRoot = candidate.path.startsWith(canonicalRoot.path + File.separator)
            candidate.takeIf { insideRoot && it.isFile }
        }.getOrNull()
    }

    fun contentUri(relativePath: String?): Uri? {
        val file = resolve(relativePath) ?: return null
        return runCatching {
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        }.getOrNull()
    }

    fun delete(relativePath: String?) {
        resolve(relativePath)?.delete()
    }

    fun deleteThreadDirectory(threadId: Long) {
        runCatching {
            val directory = File(root, threadId.toString()).canonicalFile
            val canonicalRoot = root.canonicalFile
            if (directory.parentFile == canonicalRoot) {
                directory.deleteRecursively()
            }
        }
    }

    fun cleanOrphans(activeRelativePaths: Set<String>) {
        if (!root.exists()) return
        val activeFiles = activeRelativePaths.mapNotNull { path ->
            runCatching { File(context.filesDir, path).canonicalPath }.getOrNull()
        }.toSet()
        root.walkBottomUp().forEach { file ->
            when {
                file == root -> Unit
                file.isDirectory -> file.delete()
                file.extension == "part" ||
                    runCatching { file.canonicalPath !in activeFiles }.getOrDefault(true) -> file.delete()
            }
        }
    }

    private fun readMetadata(uri: Uri): AttachmentMetadata {
        var displayName: String? = null
        var sizeBytes: Long? = null
        context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
            null,
            null,
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIndex >= 0) displayName = cursor.getString(nameIndex)
                if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) sizeBytes = cursor.getLong(sizeIndex)
            }
        }
        return AttachmentMetadata(
            displayName = displayName?.takeIf(String::isNotBlank) ?: "未命名附件",
            mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream",
            sizeBytes = sizeBytes,
        )
    }

    private data class AttachmentMetadata(
        val displayName: String,
        val mimeType: String,
        val sizeBytes: Long?,
    )

    private companion object {
        const val ROOT_DIRECTORY = "memo_attachments"
    }
}
