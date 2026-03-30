package com.rangerscards.domain.model

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
    val campaignInfo: DeckCampaignInfo,
    val previousDeck: PreviousDeck,
    val nextId: String?,
    val deckChanges: DeckChanges = DeckChanges(),
    val oftenUpdatableDeckValues: OftenUpdatableDeckValues
)

data class PlayerInfo(
    val userId: String,
    val userName: String,
)

data class DeckMeta(
    val roleId: String,
    val background: String,
    val specialty: String,
    val problems: List<String>?,
)

data class DeckCampaignInfo(
    val campaignId: String?,
    val campaignName: String?,
    val campaignRewards: List<String>?,
)

data class PreviousDeck(
    val id: String?,
    val slots: List<DeckSlot>?,
    val sideSlots: List<DeckSlot>?,
)

data class DeckSlot(
    val id: String,
    val count: Int
)

data class DeckChanges(
    val addedCards: PersistentMap<String, Int> = persistentMapOf(),
    val removedCards: PersistentMap<String, Int> = persistentMapOf(),
    val addedCollectionCards: PersistentMap<String, Int> = persistentMapOf(),
    val returnedCollectionCards: PersistentMap<String, Int> = persistentMapOf(),
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
