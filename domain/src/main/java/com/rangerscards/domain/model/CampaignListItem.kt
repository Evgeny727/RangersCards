package com.rangerscards.domain.model

data class CampaignListItem(
    val id: String,
    val cycleId: String,
    val name: String,
    val day: Int,
    val currentLocation: String,
    val latestDecksRoles: List<String>,
    val players: List<String>,
)
