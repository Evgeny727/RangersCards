package com.rangerscards.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.rangerscards.data.local.campaign.Campaign
import com.rangerscards.data.local.campaign.CampaignListItemProjection
import com.rangerscards.data.local.dao.CampaignDao
import com.rangerscards.data.local.dao.DeckDao
import com.rangerscards.data.local.deck.Deck
import com.rangerscards.data.local.deck.RoleCardProjection
import kotlinx.coroutines.flow.Flow

class OfflineCampaignsRepository(
    private val campaignDao: CampaignDao,
    private val deckDao: DeckDao
) : CampaignsRepository {
    override suspend fun deleteAllUploadedCampaigns() = campaignDao.deleteAllUploadedCampaigns()

    override suspend fun syncCampaigns(networkCampaigns: List<Campaign>) =
        campaignDao.syncCampaigns(networkCampaigns)

    override suspend fun upsertCampaigns(campaigns: List<Campaign>) =
        campaignDao.upsertAllCampaigns(campaigns)

    override suspend fun getCampaignById(id: String): Campaign = campaignDao.getCampaignById(id)

    override fun getAllCampaigns(): Flow<PagingData<CampaignListItemProjection>> {
        // Create a Pager that wraps the PagingSource from the DAO.
        return Pager(
            config = PagingConfig(
                pageSize = 5,
                enablePlaceholders = false,
                initialLoadSize = 10
            ),
            pagingSourceFactory = { campaignDao.getAllCampaigns() }
        ).flow
    }

    override fun searchCampaigns(query: String): Flow<PagingData<CampaignListItemProjection>> {
        val newQuery = query
            .lowercase()
            .replace("\"(\\[\"]|.*)?\"".toRegex(), " ")
            .split("[^\\p{Alnum}]+".toRegex())
            .filter { it.isNotBlank() }
            .joinToString(separator = " ", transform = { "%$it%" })
        // Create a Pager that wraps the PagingSource from the DAO.
        return Pager(
            config = PagingConfig(
                pageSize = 5,
                enablePlaceholders = false,
                initialLoadSize = 10
            ),
            pagingSourceFactory = { campaignDao.searchCampaigns(newQuery) }
        ).flow
    }

    override fun getAllCampaignsForTransfer(cycleId: String, userId: String): Flow<List<CampaignListItemProjection>> =
        campaignDao.getAllCampaignsForTransfer(cycleId,  userId)

    override fun getRolesImages(ids: List<String>): Flow<List<RoleCardProjection>> = campaignDao.getRolesImages(ids)

    override suspend fun insertCampaign(campaign: Campaign) = campaignDao.insertCampaign(campaign)

    override suspend fun insertDecks(decks: List<Deck>) = deckDao.upsertAllDecks(decks)
}