package com.rangerscards.data.local.deck

import androidx.room.ColumnInfo
import kotlinx.serialization.json.JsonElement

data class DeckListItemProjection(
    val id: String,
    @ColumnInfo(name = "user_handle")
    val userHandle: String?,
    val name: String,
    val meta: JsonElement,
    @ColumnInfo(name = "campaign_name")
    val campaignName: String?,
)
