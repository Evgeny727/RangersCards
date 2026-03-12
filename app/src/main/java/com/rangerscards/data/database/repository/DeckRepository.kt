package com.rangerscards.data.database.repository

import androidx.paging.PagingData
import com.rangerscards.data.CardFilterOptions
import com.rangerscards.data.database.card.CardDeckListItemProjection
import com.rangerscards.data.database.deck.Deck
import com.rangerscards.data.database.deck.RoleCardProjection
import com.rangerscards.ui.deck.DeckInfo
import kotlinx.coroutines.flow.Flow

interface DeckRepository {

    suspend fun getDeck(id: String): Deck?

    suspend fun getRole(code: String, taboo: Boolean): RoleCardProjection?

    suspend fun updateDeck(deck: Deck)

    suspend fun insertDeck(deck: Deck)

    suspend fun upsertDeck(deck: Deck)

    suspend fun upsertDecks(decks: List<Deck>)

    suspend fun deleteDeckById(id: String)

    suspend fun deleteDecksById(ids: List<String>)

    fun getCardsByIds(ids: List<String>, tabooId: String?): Flow<List<CardDeckListItemProjection>>

    suspend fun getChangedCardsByIds(ids: List<String>, tabooId: String?): List<CardDeckListItemProjection>

    fun getAllCards(
        deckInfo: DeckInfo,
        typeIndex: Int,
        showAllSpoilers: Boolean,
        packIds: List<String>,
        filterOptions: CardFilterOptions
    ): Flow<PagingData<CardDeckListItemProjection>>

    fun searchCards(
        filterOptions: CardFilterOptions,
        deckInfo: DeckInfo,
        includeEnglish: Boolean,
        typeIndex: Int,
        showAllSpoilers: Boolean,
        language: String,
        packIds: List<String>
    ): Flow<PagingData<CardDeckListItemProjection>>

    suspend fun getAllVersionDeckIds(startId: String): List<String>
}