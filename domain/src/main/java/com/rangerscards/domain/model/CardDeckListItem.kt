package com.rangerscards.domain.model

data class CardDeckListItem(
    val id: String,
    val code: String,
    val tabooId: String?,
    val name: String?,
    val approaches: CardApproaches?,
    val traits: String?,
    val equip:  Int?,
    val realTraits: String?,
    val setName: String?,
    val level: Int?,
    val typeName: String?,
    val cost: Int?,
    val aspect: CardAspect?,
    val realImageSrc: String?,
    val setId: String?,
    val setTypeId: String?,
    val deckLimit: Int?,
)
