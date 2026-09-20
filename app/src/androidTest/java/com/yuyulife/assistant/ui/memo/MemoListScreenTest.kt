package com.yuyulife.assistant.ui.memo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.yuyulife.assistant.ui.theme.YuyuLifeTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class MemoListScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emptyListOpensCreateDialog() {
        composeRule.setContent {
            YuyuLifeTheme {
                MemoListScreen(
                    uiState = MemoListUiState(),
                    onCreate = {},
                    onOpen = {},
                    onDelete = {},
                )
            }
        }

        composeRule.onNodeWithText("还没有备忘录").assertIsDisplayed()
        composeRule.onNodeWithText("新建").performClick()
        composeRule.onNodeWithText("新建备忘录").assertIsDisplayed()
    }
}
