package com.rangerscards.domain.model

import kotlinx.collections.immutable.ImmutableList

data class CampaignListItem(
    val id: String,
    val cycleId: String,
    val name: String,
    val day: Int,
    val currentLocation: String,
    val latestDecksRoles: ImmutableList<String>,
    val players: ImmutableList<String>,
)
