package com.yuyulife.assistant.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.yuyulife.assistant.data.repository.LedgerRepository
import com.yuyulife.assistant.data.repository.TodoRepository
import com.yuyulife.assistant.ui.ledger.LedgerRoute
import com.yuyulife.assistant.ui.theme.YuyuLifeTheme
import com.yuyulife.assistant.ui.todo.TodoRoute

@Composable
fun YuyuLifeApp(
    todoRepository: TodoRepository,
    ledgerRepository: LedgerRepository,
) {
    YuyuLifeTheme {
        var currentSection by rememberSaveable { mutableStateOf(AppSection.TODO) }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar {
                    AppSection.entries.forEach { section ->
                        NavigationBarItem(
                            selected = currentSection == section,
                            onClick = { currentSection = section },
                            icon = { Text(section.symbol) },
                            label = { Text(section.label) },
                        )
                    }
                }
            },
        ) { contentPadding ->
            when (currentSection) {
                AppSection.TODO -> TodoRoute(
                    repository = todoRepository,
                    modifier = Modifier.padding(contentPadding),
                )

                AppSection.LEDGER -> LedgerRoute(
                    repository = ledgerRepository,
                    modifier = Modifier.padding(contentPadding),
                )
            }
        }
    }
}

