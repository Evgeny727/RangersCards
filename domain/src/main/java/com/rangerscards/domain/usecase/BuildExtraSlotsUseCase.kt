package com.rangerscards.domain.usecase

import com.rangerscards.domain.model.CardDeckListItem
import com.rangerscards.domain.model.CardWithCount
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

class BuildExtraSlotsUseCase {
    operator fun invoke(
        cards: List<CardDeckListItem>,
        values: Map<String, Int>
    ): ImmutableList<CardWithCount> {
        return cards.map { card ->
            CardWithCount(
                card = card,
                count = values[card.code] ?: 0
            )
        }.toImmutableList()
    }
}