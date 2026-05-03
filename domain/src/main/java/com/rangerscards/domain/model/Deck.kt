package com.rangerscards.domain.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf

data class Deck(
    val id: String,
    val uploaded: Boolean,
    val playerInfo: PlayerInfo,
    val tabooSetId: String?,
    val version: Int,
    val name: String,
    val description: String?,
    val deckMeta: DeckMeta,
    val createdAt: String?,
    val updatedAt: String?,
    val campaignInfo: DeckCampaignInfo?,
    val previousDeck: PreviousDeck?,
    val nextId: String?,
    val oftenUpdatableDeckValues: OftenUpdatableDeckValues
)

data class PlayerInfo(
    val id: String,
    val name: String,
)

data class DeckMeta(
    val roleId: String,
    val background: String,
    val specialty: String,
    val problems: ImmutableList<String>? = null,
)

data class DeckCampaignInfo(
    val campaignId: String,
    val campaignName: String,
    val campaignRewards: ImmutableList<String>,
)

data class PreviousDeck(
    val id: String,
    val slots: ImmutableMap<String, Int>,
    val sideSlots: ImmutableMap<String, Int>,
)

data class DeckChanges(
    val addedCards: ImmutableMap<String, Int> = persistentMapOf(),
    val removedCards: ImmutableMap<String, Int> = persistentMapOf(),
    val addedCollectionCards: ImmutableMap<String, Int> = persistentMapOf(),
    val returnedCollectionCards: ImmutableMap<String, Int> = persistentMapOf(),
)

data class OftenUpdatableDeckValues(
    val slots: PersistentMap<String, Int>,
    val sideSlots: PersistentMap<String, Int>,
    val extraSlots: PersistentMap<String, Int>,
    val awa: Int,
    val spi: Int,
    val fit: Int,
    val foc: Int,
)

data class DeckInfo(
    val isUpgrade: Boolean,
    val background: String,
    val specialty: String,
    val rewards: List<String>,
    val extraSlots: List<String>,
    val taboo: String?,
)

data class CardWithCount(
    val card: CardDeckListItem,
    val count: Int
)