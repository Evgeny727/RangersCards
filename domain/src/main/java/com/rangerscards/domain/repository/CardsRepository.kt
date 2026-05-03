package com.rangerscards.domain.repository

import androidx.paging.PagingData
import com.rangerscards.domain.model.CardDeckListItem
import com.rangerscards.domain.model.CardFilterOptions
import com.rangerscards.domain.model.CardListItem
import com.rangerscards.domain.model.DeckInfo
import com.rangerscards.domain.model.FullCard
import com.rangerscards.domain.model.RoleCard
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow

interface CardsRepository {

    suspend fun downloadAllCards(locale: String): Result<String>

    suspend fun isCardsTableExists(): Boolean

    suspend fun isCardsUpdateAvailable(locale: String, savedTimestamp: String): Result<Boolean>

    fun searchPaginatedCardsFlow(
        filterOptions: CardFilterOptions,
        spoiler: Boolean,
        taboo: Boolean,
        packIds: List<String>,
        includeEnglish: Boolean? = null,
    ): Flow<PagingData<CardListItem>>

    fun getCardByCodeFlow(cardCode: String, taboo: Boolean): Flow<FullCard>

    fun getRoleCardByCodeFlow(code: String, taboo: Boolean): Flow<RoleCard>

    fun getAllPaginatedRoleCardsFlow(specialty: String, taboo: Boolean, packIds: List<String>): Flow<PagingData<RoleCard>>

    fun getRoleCardsByIdFlow(ids: List<String>): Flow<List<RoleCard>>

    fun getDeckCardsByIdFlow(ids: List<String>, tabooId: String?): Flow<ImmutableList<CardDeckListItem>>

    suspend fun getChangedDeckCardsById(ids: List<String>, tabooId: String?): List<CardDeckListItem>

    fun searchPaginatedDeckCardsFlow(
        filterOptions: CardFilterOptions,
        deckInfo: DeckInfo,
        typeIndex: Int,
        showAllSpoilers: Boolean,
        packIds: List<String>,
        includeEnglish: Boolean? = null,
    ): Flow<PagingData<CardDeckListItem>>

    fun getRewards(query: String, taboo: Boolean, packIds: List<String>): Flow<ImmutableList<CardListItem>>
}