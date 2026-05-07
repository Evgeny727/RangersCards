package com.rangerscards.data.local.card

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.FtsOptions
import androidx.room.PrimaryKey

@Fts4(tokenizer = FtsOptions.TOKENIZER_UNICODE61, contentEntity = Card::class)
@Entity(tableName = "Card_fts")
data class CardFts(
    @PrimaryKey
    @ColumnInfo(name = "rowid")
    val rowId: Int,
    val id: String,
    val composite: String?,
    @ColumnInfo(name = "real_composite")
    val realComposite: String?
)
