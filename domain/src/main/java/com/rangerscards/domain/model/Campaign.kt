package com.rangerscards.domain.model

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
    val missions: List<CampaignMission>,
    val events: List<CampaignEvent>,
    val rewards: List<String>,
    val removed: List<CampaignRemoved>,
    val history: List<CampaignHistory>,
    val calendar: List<CampaignCalendar>,
    val expansions: List<String>,
    val decks: List<CampaignDeck>,
    val access: List<PlayerInfo>,
    val previousCampaignId: String?,
    val nextCampaignId: String?
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
    val travel: List<CampaignHistory>
)

data class CampaignCalendar(
    val day: Int,
    val guides: List<String>,
)

data class CampaignDeck(
    val id: String,
    val name: String,
    val roleId: String,
    val meta: DeckMeta,
    val user: PlayerInfo,
)
