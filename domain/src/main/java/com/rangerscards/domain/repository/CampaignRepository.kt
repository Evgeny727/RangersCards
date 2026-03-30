package com.rangerscards.domain.repository

import androidx.paging.PagingData
import com.rangerscards.domain.model.Campaign
import com.rangerscards.domain.model.CampaignListItem
import com.rangerscards.domain.model.CardListItem
import com.rangerscards.domain.model.Deck
import com.rangerscards.domain.model.DeckListItem
import com.rangerscards.domain.model.FullCard
import com.rangerscards.domain.model.RoleCard
import kotlinx.coroutines.flow.Flow

interface CampaignRepository {

    suspend fun updateCampaign(campaign: Campaign)

    suspend fun insertCampaign(campaign: Campaign)

    suspend fun upsertChallengeDeck(campaignId: String, challengeDeckIds: List<String>)

    fun getCampaignFlowById(id: String): Flow<Campaign?>

    suspend fun getCampaignById(id: String): Result<Campaign>

    fun getCampaignChallengeDeckFlowById(campaignId: String): Flow<List<String>?>

    fun getRoleFlow(id: String, taboo: Boolean): Flow<RoleCard?>

    fun getAllDecksFlow(userId: String, uploaded: Boolean): Flow<PagingData<DeckListItem>>

    fun searchDecks(query: String, userId: String, uploaded: Boolean): Flow<PagingData<DeckListItem>>

    suspend fun deleteCampaign(id: String)

    fun getRewards(taboo: Boolean, packIds: List<String>): Flow<List<CardListItem>>

    fun getCardById(cardCode: String, taboo: Boolean): Flow<FullCard>

    suspend fun deleteAllUploadedCampaigns()

    suspend fun syncCampaigns(networkCampaigns: List<Campaign>)

    suspend fun upsertCampaigns(campaigns: List<Campaign>)

    fun getAllCampaigns(): Flow<PagingData<CampaignListItem>>

    fun searchCampaigns(query: String): Flow<PagingData<CampaignListItem>>

    fun getAllCampaignsForTransfer(cycleId: String, userId: String): Flow<List<CampaignListItem>>

    fun getRolesImages(ids: List<String>): Flow<List<RoleCard>>

    suspend fun insertDecks(decks: List<Deck>)
}