package com.rangerscards.data.local.campaign

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.json.JsonElement

@Entity(tableName = "Campaign")
data class Campaign(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val uploaded: Boolean,
    @ColumnInfo(name = "user_id")
    val userId: String,
    val name: String,
    val notes: JsonElement,
    val day: Int,
    @ColumnInfo(name = "extended_calendar")
    val extendedCalendar: Boolean?,
    @ColumnInfo(name = "cycle_id")
    val cycleId: String,
    @ColumnInfo(name = "current_location")
    val currentLocation: String,
    @ColumnInfo(name = "current_path_terrain")
    val currentPathTerrain: String?,
    val missions: JsonElement,
    val events: JsonElement,
    val rewards: JsonElement,
    val removed: JsonElement,
    val history: JsonElement,
    val calendar: JsonElement,
    @ColumnInfo(name = "created_at")
    val createdAt: String?,
    @ColumnInfo(name = "updated_at")
    val updatedAt: String?,
    val expansions: JsonElement,
    @ColumnInfo(name = "latest_decks")
    val latestDecks: JsonElement,
    val access: JsonElement,
    @ColumnInfo(name = "next_campaign_id")
    val nextCampaignId: String?,
    @ColumnInfo(name = "previous_campaign_id")
    val previousCampaignId: String?,
)
