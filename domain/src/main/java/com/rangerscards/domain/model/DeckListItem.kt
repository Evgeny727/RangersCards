package com.rangerscards.domain.model

data class DeckListItem(
    val id: String,
    val userHandle: String?,
    val name: String,
    val meta: DeckMeta,
    val campaignName: String?,
)
