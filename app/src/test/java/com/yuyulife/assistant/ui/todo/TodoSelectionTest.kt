package com.yuyulife.assistant.ui.todo

import org.junit.Assert.assertEquals
import org.junit.Test

class TodoSelectionTest {
    @Test
    fun `toggle adds an unselected item`() {
        assertEquals(setOf(1L, 2L), toggleSelection(setOf(1L), 2L))
    }

    @Test
    fun `toggle removes a selected item`() {
        assertEquals(setOf(1L), toggleSelection(setOf(1L, 2L), 2L))
    }
}
