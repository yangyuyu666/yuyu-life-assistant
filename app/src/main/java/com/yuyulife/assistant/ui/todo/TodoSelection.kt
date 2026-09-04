package com.yuyulife.assistant.ui.todo

internal fun toggleSelection(current: Set<Long>, itemId: Long): Set<Long> =
    current.toMutableSet().apply {
        if (!add(itemId)) remove(itemId)
    }

internal fun toggleSelectAll(current: Set<Long>, allIds: Set<Long>): Set<Long> =
    if (allIds.isNotEmpty() && current.containsAll(allIds)) emptySet() else allIds
