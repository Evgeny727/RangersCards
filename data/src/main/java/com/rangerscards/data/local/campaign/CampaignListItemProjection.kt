package com.rangerscards.data.local.campaign

import androidx.room.ColumnInfo
import kotlinx.serialization.json.JsonElement

data class CampaignListItemProjection(
    val id: String,
    @ColumnInfo(name = "cycle_id")
    val cycleId: String,
    val name: String,
    val day: Int,
    @ColumnInfo(name = "current_location")
    val currentLocation: String,
    @ColumnInfo(name = "latest_decks")
    val latestDecks: JsonElement,
    val access: JsonElement,
)
