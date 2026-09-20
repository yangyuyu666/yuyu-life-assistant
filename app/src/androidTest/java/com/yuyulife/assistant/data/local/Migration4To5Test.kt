package com.yuyulife.assistant.data.local

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Migration4To5Test {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val databaseName = "migration-4-5-test.db"

    @After
    fun cleanUp() {
        context.deleteDatabase(databaseName)
    }

    @Test
    fun migrationPreservesExistingDataAndAddsCascadingMemoTables() {
        createVersion4Database().close()
        val helper = openVersion5Database()
        val database = helper.writableDatabase

        database.query("SELECT title, deadlineAt FROM todos WHERE id = 2").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("旧待办", cursor.getString(0))
            assertEquals(987654L, cursor.getLong(1))
        }
        database.query("SELECT amountCents, note FROM ledger_entries WHERE id = 3").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(2500L, cursor.getLong(0))
            assertEquals("旧账目", cursor.getString(1))
        }

        database.execSQL(
            "INSERT INTO memo_threads (id, title, createdAt, updatedAt) " +
                "VALUES (11, '旅行资料', 100, 100)",
        )
        database.execSQL(
            "INSERT INTO memo_messages " +
                "(id, threadId, kind, text, attachmentPath, attachmentDisplayName, " +
                "attachmentMimeType, attachmentSizeBytes, createdAt) " +
                "VALUES (12, 11, 'TEXT', '准备出发', NULL, NULL, NULL, NULL, 101)",
        )
        database.execSQL("DELETE FROM memo_threads WHERE id = 11")
        database.query("SELECT id FROM memo_messages WHERE id = 12").use { cursor ->
            assertFalse(cursor.moveToFirst())
        }
        helper.close()
    }

    private fun createVersion4Database(): SupportSQLiteOpenHelper {
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(databaseName)
                .callback(object : SupportSQLiteOpenHelper.Callback(4) {
                    override fun onConfigure(db: SupportSQLiteDatabase) {
                        db.setForeignKeyConstraintsEnabled(true)
                    }

                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL(
                            "CREATE TABLE todos (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                                "title TEXT NOT NULL, createdAt INTEGER NOT NULL, deadlineAt INTEGER)",
                        )
                        db.execSQL(
                            "CREATE TABLE ledger_categories (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                                "type TEXT NOT NULL, name TEXT NOT NULL, sortOrder INTEGER NOT NULL)",
                        )
                        db.execSQL(
                            "CREATE UNIQUE INDEX index_ledger_categories_type_name " +
                                "ON ledger_categories (type, name)",
                        )
                        db.execSQL(
                            "CREATE TABLE ledger_entries (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                                "amountCents INTEGER NOT NULL, categoryId INTEGER NOT NULL, " +
                                "note TEXT NOT NULL, occurredAt INTEGER NOT NULL, " +
                                "FOREIGN KEY(categoryId) REFERENCES ledger_categories(id) " +
                                "ON UPDATE NO ACTION ON DELETE RESTRICT)",
                        )
                        db.execSQL("CREATE INDEX index_ledger_entries_categoryId ON ledger_entries (categoryId)")
                        db.execSQL(
                            "INSERT INTO todos (id, title, createdAt, deadlineAt) " +
                                "VALUES (2, '旧待办', 123456, 987654)",
                        )
                        db.execSQL(
                            "INSERT INTO ledger_categories (id, type, name, sortOrder) " +
                                "VALUES (1, 'EXPENSE', '餐饮', 0)",
                        )
                        db.execSQL(
                            "INSERT INTO ledger_entries " +
                                "(id, amountCents, categoryId, note, occurredAt) " +
                                "VALUES (3, 2500, 1, '旧账目', 456789)",
                        )
                    }

                    override fun onUpgrade(
                        db: SupportSQLiteDatabase,
                        oldVersion: Int,
                        newVersion: Int,
                    ) = Unit
                })
                .build(),
        )
        helper.writableDatabase
        return helper
    }

    private fun openVersion5Database(): SupportSQLiteOpenHelper =
        FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(databaseName)
                .callback(object : SupportSQLiteOpenHelper.Callback(5) {
                    override fun onConfigure(db: SupportSQLiteDatabase) {
                        db.setForeignKeyConstraintsEnabled(true)
                    }

                    override fun onCreate(db: SupportSQLiteDatabase) = Unit

                    override fun onUpgrade(
                        db: SupportSQLiteDatabase,
                        oldVersion: Int,
                        newVersion: Int,
                    ) {
                        MIGRATION_4_5.migrate(db)
                    }
                })
                .build(),
        )
}
