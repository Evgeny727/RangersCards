package com.rangerscards.domain.model

data class StarterDeck(
    val meta: StarterDeckMeta,
    val foc: Int,
    val spi: Int,
    val awa: Int,
    val fit: Int,
    val slots: List<DeckSlot>,
)

data class StarterDeckMeta(
    val role: String,
    val background: String,
    val specialty: String
)