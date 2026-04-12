package com.rangerscards.domain.model

import kotlinx.collections.immutable.ImmutableList

data class StarterDeck(
    val meta: DeckMeta,
    val foc: Int,
    val spi: Int,
    val awa: Int,
    val fit: Int,
    val slots: ImmutableList<DeckSlot>,
)