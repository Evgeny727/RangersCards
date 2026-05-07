package com.rangerscards.data.local.campaign

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement

@Entity(tableName = "Challenge_deck", foreignKeys = [
    ForeignKey(
        entity = Campaign::class,
        parentColumns = ["id"],
        childColumns = ["id"],
        onDelete = ForeignKey.Companion.CASCADE,
    )
])
data class ChallengeDeck(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    @ColumnInfo(name = "challenge_deck_ids")
    val challengeDeckIds: JsonElement = JsonArray(emptyList())
)