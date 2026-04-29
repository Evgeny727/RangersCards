package com.rangerscards.domain.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

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
    val slots: ImmutableList<DeckSlot>,
    val sideSlots: ImmutableList<DeckSlot>,
)

data class DeckSlot(
    val id: String,
    val count: Int
)

data class DeckChanges(
    val addedCards: ImmutableList<DeckSlot> = persistentListOf(),
    val removedCards: ImmutableList<DeckSlot> = persistentListOf(),
    val addedCollectionCards: ImmutableList<DeckSlot> = persistentListOf(),
    val returnedCollectionCards: ImmutableList<DeckSlot> = persistentListOf(),
)

data class OftenUpdatableDeckValues(
    val slots: PersistentList<DeckSlot>,
    val sideSlots: PersistentList<DeckSlot>,
    val extraSlots: PersistentList<DeckSlot>,
    val awa: Int,
    val spi: Int,
    val fit: Int,
    val foc: Int,
)

data class DeckInfo(
    val isUpgrade: Boolean,
    val background: String,
    val specialty: String,
    val rewards: ImmutableList<String>,
    val extraSlots: ImmutableList<String>,
    val taboo: String?,
)