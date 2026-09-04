package com.yuyulife.assistant.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.yuyulife.assistant.data.local.LedgerCategoryDao
import com.yuyulife.assistant.data.local.LedgerCategoryEntity
import com.yuyulife.assistant.domain.model.LedgerCategory
import com.yuyulife.assistant.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LedgerCategoryRepository(
    private val dao: LedgerCategoryDao,
) {
    val categories: Flow<List<LedgerCategory>> = dao.observeAllWithUsage().map { records ->
        records.map { it.toDomain() }
    }

    suspend fun add(type: TransactionType, rawName: String): CategoryChangeResult {
        val name = rawName.trim()
        if (name.isEmpty() || name.length > MAX_NAME_LENGTH) return CategoryChangeResult.INVALID_NAME
        if (dao.countByName(type.name, name) > 0) return CategoryChangeResult.DUPLICATE
        return try {
            dao.insert(
                LedgerCategoryEntity(
                    type = type.name,
                    name = name,
                    sortOrder = dao.nextSortOrder(type.name),
                ),
            )
            CategoryChangeResult.SUCCESS
        } catch (_: SQLiteConstraintException) {
            CategoryChangeResult.DUPLICATE
        }
    }

    suspend fun rename(id: Long, rawName: String): CategoryChangeResult {
        val name = rawName.trim()
        if (name.isEmpty() || name.length > MAX_NAME_LENGTH) return CategoryChangeResult.INVALID_NAME
        val category = dao.findById(id) ?: return CategoryChangeResult.NOT_FOUND
        if (dao.countByName(category.type, name, id) > 0) return CategoryChangeResult.DUPLICATE
        return try {
            if (dao.rename(id, name) > 0) CategoryChangeResult.SUCCESS else CategoryChangeResult.NOT_FOUND
        } catch (_: SQLiteConstraintException) {
            CategoryChangeResult.DUPLICATE
        }
    }

    suspend fun delete(id: Long): CategoryChangeResult = try {
        when {
            dao.usageCount(id) > 0 -> CategoryChangeResult.IN_USE
            dao.deleteIfUnused(id) -> CategoryChangeResult.SUCCESS
            else -> CategoryChangeResult.NOT_FOUND
        }
    } catch (_: SQLiteConstraintException) {
        CategoryChangeResult.IN_USE
    }

    companion object {
        const val MAX_NAME_LENGTH = 20
    }
}

enum class CategoryChangeResult {
    SUCCESS,
    INVALID_NAME,
    DUPLICATE,
    IN_USE,
    NOT_FOUND,
}
