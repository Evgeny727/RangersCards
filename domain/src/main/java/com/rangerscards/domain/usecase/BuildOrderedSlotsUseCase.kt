package com.rangerscards.domain.usecase

import com.rangerscards.domain.model.CardDeckListItem
import com.rangerscards.domain.model.CardWithCount
import com.rangerscards.domain.model.DeckMeta
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap

class BuildOrderedSlotsUseCase {
    operator fun invoke(
        cards: List<CardDeckListItem>,
        values: Map<String, Int>,
        deckMeta: DeckMeta?
    ): ImmutableMap<String, ImmutableList<CardWithCount>> {
        val grouped = cards.groupBy { card ->
            when {
                card.setId == "personality" -> "personality"
                card.setTypeId == "background" ->
                    if (card.setId == deckMeta?.background) "background" else "outsideInterest"
                card.setTypeId == "specialty" ->
                    if (card.setId == deckMeta?.specialty) "specialty" else "outsideInterest"
                else -> "other"
            }
        }

        val orderedKeys = listOf(
            "personality",
            "background",
            "specialty",
            "outsideInterest",
            "other"
        )

        return orderedKeys.associateWith { key ->
            grouped[key].orEmpty().map { card ->
                CardWithCount(
                    card = card,
                    count = values[card.code] ?: 0
                )
            }.toImmutableList()
        }.toImmutableMap()
    }
}