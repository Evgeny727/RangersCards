package com.rangerscards.domain.repository

import androidx.paging.PagingData
import com.rangerscards.domain.model.Deck
import com.rangerscards.domain.model.DeckCampaignInfo
import com.rangerscards.domain.model.DeckListItem
import com.rangerscards.domain.model.DeckMeta
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.flow.Flow


interface DecksRepository {

    suspend fun deleteAllLocalDecks()

    suspend fun syncDecks(userId: String): Result<Unit>

    suspend fun syncDeckById(id: Int): Result<Unit>

    fun getAllPaginatedDecksFlow(userId: String, uploaded: Boolean? = null): Flow<PagingData<DeckListItem>>

    fun searchPaginatedDecksFlow(query: String, userId: String, uploaded: Boolean? = null): Flow<PagingData<DeckListItem>>

    suspend fun createDeck(
        uploaded: Boolean,
        name: String,
        slots: ImmutableMap<String, Int>,
        extraSlots: ImmutableMap<String, Int> = persistentMapOf(),
        meta: DeckMeta,
        tabooSetId: String?,
        awa: Int? = null,
        spi: Int? = null,
        fit: Int? = null,
        foc: Int? = null,
        ): Result<String>

    fun getDeckByIdFlow(id: String): Flow<Deck>

    suspend fun upgradeDeck(id: String, uploaded: Boolean): Result<String>

    suspend fun saveDeck(deck: Deck): Result<Unit>

    suspend fun saveDeckTabooSet(id: String, tabooId: String?, uploaded: Boolean): Result<Unit>

    suspend fun setDeckCampaign(id: String, campaignInfo: DeckCampaignInfo, uploaded: Boolean): Result<Unit>

    suspend fun removeDeckCampaign(id: String, campaignInfo: DeckCampaignInfo, uploaded: Boolean): Result<Unit>

    suspend fun deleteDeckById(id: String, uploaded: Boolean): Result<String?>

    suspend fun deleteAllDeckVersionsById(id: String, uploaded: Boolean): Result<Unit>

    suspend fun getAllDeckVersionIds(startId: String): ImmutableList<String>
}