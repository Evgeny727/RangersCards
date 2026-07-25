package com.rangerscards.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rangerscards.data.remote.patches.CardMulligan

object MigrationCardSetupField : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            ALTER TABLE Card
            ADD COLUMN setup INTEGER NOT NULL DEFAULT 0
        """.trimIndent())

        val codes = CardMulligan.setupField.joinToString { "'$it'" }

        db.execSQL("""
            UPDATE Card
            SET setup = 1
            WHERE code IN ($codes)
        """.trimIndent())
    }
}