package com.yuyulife.assistant.data.local

import androidx.room.migration.Migration
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE todos ADD COLUMN deadlineAt INTEGER")
    }
}

val DATABASE_CREATE_CALLBACK = object : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        seedDefaultLedgerCategories(db)
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE TABLE todos_new (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "title TEXT NOT NULL, " +
                "createdAt INTEGER NOT NULL, " +
                "deadlineAt INTEGER)",
        )
        database.execSQL(
            "INSERT INTO todos_new (id, title, createdAt, deadlineAt) " +
                "SELECT id, title, createdAt, deadlineAt FROM todos",
        )
        database.execSQL("DROP TABLE todos")
        database.execSQL("ALTER TABLE todos_new RENAME TO todos")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS ledger_categories (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "type TEXT NOT NULL, " +
                "name TEXT NOT NULL, " +
                "sortOrder INTEGER NOT NULL)",
        )
        database.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS index_ledger_categories_type_name " +
                "ON ledger_categories (type, name)",
        )

        seedDefaultLedgerCategories(database)
        database.execSQL(
            "INSERT OR IGNORE INTO ledger_categories (type, name, sortOrder) " +
                "SELECT DISTINCT type, category, 1000 FROM ledger_entries",
        )

        database.execSQL(
            "CREATE TABLE ledger_entries_new (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "amountCents INTEGER NOT NULL, " +
                "categoryId INTEGER NOT NULL, " +
                "note TEXT NOT NULL, " +
                "occurredAt INTEGER NOT NULL, " +
                "FOREIGN KEY(categoryId) REFERENCES ledger_categories(id) " +
                "ON UPDATE NO ACTION ON DELETE RESTRICT)",
        )
        database.execSQL(
            "INSERT INTO ledger_entries_new (id, amountCents, categoryId, note, occurredAt) " +
                "SELECT e.id, e.amountCents, c.id, e.note, e.occurredAt " +
                "FROM ledger_entries e " +
                "INNER JOIN ledger_categories c ON c.type = e.type AND c.name = e.category",
        )
        database.execSQL("DROP TABLE ledger_entries")
        database.execSQL("ALTER TABLE ledger_entries_new RENAME TO ledger_entries")
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS index_ledger_entries_categoryId " +
                "ON ledger_entries (categoryId)",
        )
    }
}

private fun seedDefaultLedgerCategories(database: SupportSQLiteDatabase) {
    val defaults = listOf(
        Triple("EXPENSE", "餐饮", 0),
        Triple("EXPENSE", "交通", 1),
        Triple("EXPENSE", "购物", 2),
        Triple("EXPENSE", "居住", 3),
        Triple("EXPENSE", "娱乐", 4),
        Triple("EXPENSE", "其他", 5),
        Triple("INCOME", "工资", 0),
        Triple("INCOME", "奖金", 1),
        Triple("INCOME", "退款", 2),
        Triple("INCOME", "其他", 3),
    )
    defaults.forEach { (type, name, order) ->
        database.execSQL(
            "INSERT OR IGNORE INTO ledger_categories (type, name, sortOrder) VALUES (?, ?, ?)",
            arrayOf<Any?>(type, name, order),
        )
    }
}
