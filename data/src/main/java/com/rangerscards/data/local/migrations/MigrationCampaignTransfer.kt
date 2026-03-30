package com.rangerscards.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object MigrationCampaignTransfer : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            ALTER TABLE Campaign
            ADD COLUMN next_campaign_id TEXT
        """.trimIndent())

        db.execSQL("""
            ALTER TABLE Campaign
            ADD COLUMN previous_campaign_id TEXT
        """.trimIndent())
    }
}