package com.rangerscards.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object MigrationCampaignChallengeDeck : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
          CREATE TABLE IF NOT EXISTS `Challenge_deck` (
            `id` TEXT NOT NULL PRIMARY KEY,
            `challenge_deck_ids` TEXT NOT NULL DEFAULT '[]',
            FOREIGN KEY(`id`) REFERENCES `Campaign`(`id`) ON DELETE CASCADE
          )
        """.trimIndent())
    }
}