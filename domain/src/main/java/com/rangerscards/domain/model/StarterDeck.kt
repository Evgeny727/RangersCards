package com.rangerscards.domain.model

import kotlinx.collections.immutable.ImmutableMap

data class StarterDeck(
    val meta: DeckMeta,
    val foc: Int,
    val spi: Int,
    val awa: Int,
    val fit: Int,
    val slots: ImmutableMap<String, Int>,
)