package com.yuyulife.assistant.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyFormatterTest {
    @Test
    fun `whole yuan is converted to cents`() {
        assertEquals(1_200L, parseAmountToCents("12"))
    }

    @Test
    fun `decimal amount is rounded to two places`() {
        assertEquals(1_236L, parseAmountToCents("12.355"))
    }

    @Test
    fun `commas are accepted`() {
        assertEquals(123_456L, parseAmountToCents("1,234.56"))
    }

    @Test
    fun `zero and invalid values are rejected`() {
        assertNull(parseAmountToCents("0"))
        assertNull(parseAmountToCents("hello"))
    }
}

