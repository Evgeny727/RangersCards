package com.rangerscards.domain.repository

import androidx.paging.PagingData
import com.rangerscards.domain.model.CardDeckListItem
import com.rangerscards.domain.model.CardFilterOptions
import com.rangerscards.domain.model.Deck
import com.rangerscards.domain.model.DeckCampaignInfo
import com.rangerscards.domain.model.DeckInfo
import com.rangerscards.domain.model.DeckListItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow


interface DecksRepository {

    suspend fun deleteAllLocalDecks()

    suspend fun syncDecks(userId: String)

    suspend fun syncDeckById(id: String)

    fun getAllPaginatedDecksFlow(userId: String, uploaded: Boolean? = null): Flow<PagingData<DeckListItem>>

    fun searchPaginatedDecksFlow(query: String, userId: String, uploaded: Boolean? = null): Flow<PagingData<DeckListItem>>

    suspend fun createDeck(deck: Deck)

    suspend fun getDeckById(id: String): Deck?

    suspend fun upgradeDeck(id: String, uploaded: Boolean)

    suspend fun saveDeck(deck: Deck)

    suspend fun saveDeckTabooSet(id: String, tabooId: String?, uploaded: Boolean)

    suspend fun setDeckCampaign(id: String, campaignInfo: DeckCampaignInfo, uploaded: Boolean)

    suspend fun removeDeckCampaign(id: String, uploaded: Boolean)

    suspend fun deleteDeckById(id: String, uploaded: Boolean)

    suspend fun deleteDecksById(ids: List<String>, uploaded: Boolean)

    fun getDeckCardsByIdFlow(ids: List<String>, tabooId: String?): Flow<List<CardDeckListItem>>

    suspend fun getChangedDeckCardsById(ids: List<String>, tabooId: String?): ImmutableList<CardDeckListItem>

    fun getAllPaginatedDeckCardsFlow(
        deckInfo: DeckInfo,
        typeIndex: Int,
        showAllSpoilers: Boolean,
        packIds: List<String>,
        filterOptions: CardFilterOptions
    ): Flow<PagingData<CardDeckListItem>>

    fun searchPaginatedDeckCardsFlow(
        filterOptions: CardFilterOptions,
        deckInfo: DeckInfo,
        includeEnglish: Boolean,
        typeIndex: Int,
        showAllSpoilers: Boolean,
        packIds: List<String>
    ): Flow<PagingData<CardDeckListItem>>

    suspend fun getAllDeckVersionIds(startId: String): ImmutableList<String>
}