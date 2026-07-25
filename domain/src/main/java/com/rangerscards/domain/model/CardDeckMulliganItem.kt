package com.rangerscards.domain.model

data class CardDeckMulliganItem(
    val id: String,
    val code: String,
    val tabooId: String?,
    val name: String?,
    val approaches: CardApproaches,
    val traits: String?,
    val level: Int?,
    val typeName: String?,
    val cost: Int?,
    val aspect: CardAspect?,
    val setup: Boolean,
    val imageSrc: String?,
)
