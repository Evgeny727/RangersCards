package com.rangerscards.domain.repository

import androidx.paging.PagingData
import com.rangerscards.objects.CardFilterOptions
import com.rangerscards.data.local.card.Card
import com.rangerscards.data.local.card.CardListItemProjection
import com.rangerscards.data.local.card.FullCardProjection
import kotlinx.coroutines.flow.Flow

interface CardsRepository {

    suspend fun insertAllCards(cards: List<Card>)

    suspend fun upsertAllCards(cards: List<Card>)

    suspend fun isExists(): Boolean

    fun getAllCards(
        spoiler: Boolean,
        taboo: Boolean,
        packIds: List<String>,
        filterOptions: CardFilterOptions
    ): Flow<PagingData<CardListItemProjection>>

    fun searchCards(
        filterOptions: CardFilterOptions,
        includeEnglish: Boolean,
        spoiler: Boolean,
        language: String,
        taboo: Boolean,
        packIds: List<String>
    ): Flow<PagingData<CardListItemProjection>>

    fun getCardById(cardCode: String, taboo: Boolean): Flow<FullCardProjection?>
}