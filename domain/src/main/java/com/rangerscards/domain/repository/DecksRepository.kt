package com.rangerscards.domain.repository

import androidx.paging.PagingData
import com.rangerscards.data.local.card.CardListItemProjection
import com.rangerscards.data.local.deck.Deck
import com.rangerscards.data.local.deck.DeckListItemProjection
import kotlinx.coroutines.flow.Flow


interface DecksRepository {

    suspend fun deleteAllUploadedDecks()

    suspend fun syncDecks(networkDecks: List<Deck>)

    fun getAllDecks(userId: String): Flow<PagingData<DeckListItemProjection>>

    fun searchDecks(query: String, userId: String): Flow<PagingData<DeckListItemProjection>>

    fun getCard(id: String): Flow<CardListItemProjection?>

    fun getRoles(specialty: String, taboo: Boolean, packIds: List<String>): Flow<PagingData<CardListItemProjection>>

    suspend fun insertDeck(deck: Deck)
}