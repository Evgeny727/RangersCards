package com.rangerscards.domain.model

import kotlinx.collections.immutable.ImmutableList

data class Campaign(
    val id: String,
    val uploaded: Boolean,
    val userId: String,
    val name: String,
    val currentDay: Int,
    val extendedCalendar: Boolean,
    val cycleId: String,
    val currentLocation: String,
    val currentPathTerrain: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val notes: ImmutableList<CampaignNote>,
    val missions: ImmutableList<CampaignMission>,
    val events: ImmutableList<CampaignEvent>,
    val rewards: ImmutableList<String>,
    val removed: ImmutableList<CampaignRemoved>,
    val history: ImmutableList<CampaignHistory>,
    val calendar: ImmutableList<CampaignCalendar>,
    val expansions: ImmutableList<String>,
    val decks: ImmutableList<CampaignDeck>,
    val access: ImmutableList<PlayerInfo>,
    val previousCampaignId: String?,
    val nextCampaignId: String?
)

data class CampaignNote(
    val day: Int,
    val note: String,
    val crossedOut: Boolean,
)

data class CampaignMission(
    val day: Int,
    val name: String,
    val checks: List<Boolean>,
    val completed: Boolean,
)

data class CampaignEvent(
    val name: String,
    val crossedOut: Boolean,
    val marks: Int = 0,
)

data class CampaignRemoved(
    val name: String,
    val setId: String,
)

data class CampaignHistory(
    val day: Int,
    val camped: Boolean,
    val location: String,
    val pathTerrain: String,
)

data class CampaignTravelDay(
    val day: Int,
    val startingLocation: String?,
    val travel: ImmutableList<CampaignHistory>
)

data class CampaignCalendar(
    val day: Int,
    val guides: ImmutableList<String>,
)

data class CampaignDeck(
    val id: String,
    val name: String,
    val meta: DeckMeta,
    val user: PlayerInfo,
)
