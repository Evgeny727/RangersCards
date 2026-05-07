package com.rangerscards.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object MigrationCampaignExpansions : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            ALTER TABLE Campaign
            ADD COLUMN expansions TEXT NOT NULL DEFAULT '[]'
        """.trimIndent())
    }
}