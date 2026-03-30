package com.rangerscards.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object MigrationRemoveFlavorFromFTS : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.beginTransaction()
        try {
            db.execSQL("""
                UPDATE Card
                SET composite = TRIM(
                    (CASE WHEN name IS NOT NULL AND name <> '' THEN name ELSE '' END)
                    || (CASE WHEN traits IS NOT NULL AND traits <> '' THEN ' ' || traits ELSE '' END)
                    || (CASE WHEN text IS NOT NULL AND text <> '' THEN ' ' || text ELSE '' END)
                    || (CASE WHEN type_name IS NOT NULL AND type_name <> '' THEN ' ' || type_name ELSE '' END)
                    || (CASE WHEN sun_challenge IS NOT NULL AND sun_challenge <> '' THEN ' ' || sun_challenge ELSE '' END)
                    || (CASE WHEN mountain_challenge IS NOT NULL AND mountain_challenge <> '' THEN ' ' || mountain_challenge ELSE '' END)
                    || (CASE WHEN crest_challenge IS NOT NULL AND crest_challenge <> '' THEN ' ' || crest_challenge ELSE '' END)
                )
            """.trimIndent())

            db.execSQL("""
                UPDATE Card
                SET real_composite = CASE WHEN real_composite IS NOT NULL THEN TRIM(
                    (CASE WHEN name IS NOT NULL AND name <> '' THEN name ELSE '' END)
                    || (CASE WHEN real_name IS NOT NULL AND real_name <> '' THEN ' ' || real_name ELSE '' END)
                    || (CASE WHEN traits IS NOT NULL AND traits <> '' THEN ' ' || traits ELSE '' END)
                    || (CASE WHEN real_traits IS NOT NULL AND real_traits <> '' THEN ' ' || real_traits ELSE '' END)
                    || (CASE WHEN text IS NOT NULL AND text <> '' THEN ' ' || text ELSE '' END)
                    || (CASE WHEN real_text IS NOT NULL AND real_text <> '' THEN ' ' || real_text ELSE '' END)
                    || (CASE WHEN type_name IS NOT NULL AND type_name <> '' THEN ' ' || type_name ELSE '' END)
                    || (CASE WHEN type_id IS NOT NULL AND type_id <> '' THEN ' ' || type_id ELSE '' END)
                    || (CASE WHEN sun_challenge IS NOT NULL AND sun_challenge <> '' THEN ' ' || sun_challenge ELSE '' END)
                    || (CASE WHEN mountain_challenge IS NOT NULL AND mountain_challenge <> '' THEN ' ' || mountain_challenge ELSE '' END)
                    || (CASE WHEN crest_challenge IS NOT NULL AND crest_challenge <> '' THEN ' ' || crest_challenge ELSE '' END)
                ) ELSE real_composite END
            """.trimIndent())

            db.execSQL("INSERT INTO card_fts(card_fts) VALUES('rebuild')")

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}