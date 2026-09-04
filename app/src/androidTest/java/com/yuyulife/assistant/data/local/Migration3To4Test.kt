package com.yuyulife.assistant.data.local

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Migration3To4Test {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val databaseName = "migration-3-4-test.db"

    @After
    fun cleanUp() {
        context.deleteDatabase(databaseName)
    }

    @Test
    fun migrationPreservesLedgerDataAndRestrictsUsedCategoryDeletion() {
        createVersion3Database().close()
        val helper = openVersion4Database()
        val database = helper.writableDatabase

        database.query(
            "SELECT e.id, e.amountCents, c.type, c.name, e.note, e.occurredAt " +
                "FROM ledger_entries e INNER JOIN ledger_categories c ON c.id = e.categoryId",
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(7L, cursor.getLong(0))
            assertEquals(1250L, cursor.getLong(1))
            assertEquals("EXPENSE", cursor.getString(2))
            assertEquals("餐饮", cursor.getString(3))
            assertEquals("午饭", cursor.getString(4))
            assertEquals(123456L, cursor.getLong(5))
        }

        val categoryId = database.query(
            "SELECT categoryId FROM ledger_entries WHERE id = 7",
        ).use { cursor ->
            cursor.moveToFirst()
            cursor.getLong(0)
        }
        var deletionBlocked = false
        try {
            database.execSQL("DELETE FROM ledger_categories WHERE id = ?", arrayOf(categoryId))
        } catch (_: SQLiteConstraintException) {
            deletionBlocked = true
        }
        assertTrue(deletionBlocked)
        helper.close()
    }

    private fun createVersion3Database(): SupportSQLiteOpenHelper {
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(databaseName)
                .callback(object : SupportSQLiteOpenHelper.Callback(3) {
                    override fun onConfigure(db: SupportSQLiteDatabase) {
                        db.setForeignKeyConstraintsEnabled(true)
                    }

                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL(
                            "CREATE TABLE todos (" +
                                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                                "title TEXT NOT NULL, createdAt INTEGER NOT NULL, deadlineAt INTEGER)",
                        )
                        db.execSQL(
                            "CREATE TABLE ledger_entries (" +
                                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                                "type TEXT NOT NULL, amountCents INTEGER NOT NULL, " +
                                "category TEXT NOT NULL, note TEXT NOT NULL, occurredAt INTEGER NOT NULL)",
                        )
                        db.execSQL(
                            "INSERT INTO ledger_entries " +
                                "(id, type, amountCents, category, note, occurredAt) " +
                                "VALUES (7, 'EXPENSE', 1250, '餐饮', '午饭', 123456)",
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

    private fun openVersion4Database(): SupportSQLiteOpenHelper =
        FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(databaseName)
                .callback(object : SupportSQLiteOpenHelper.Callback(4) {
                    override fun onConfigure(db: SupportSQLiteDatabase) {
                        db.setForeignKeyConstraintsEnabled(true)
                    }

                    override fun onCreate(db: SupportSQLiteDatabase) = Unit

                    override fun onUpgrade(
                        db: SupportSQLiteDatabase,
                        oldVersion: Int,
                        newVersion: Int,
                    ) {
                        MIGRATION_3_4.migrate(db)
                    }
                })
                .build(),
        )
}
