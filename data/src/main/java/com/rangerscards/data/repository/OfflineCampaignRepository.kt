package com.rangerscards.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.rangerscards.data.local.campaign.Campaign
import com.rangerscards.data.local.campaign.ChallengeDeck
import com.rangerscards.data.local.card.CardListItemProjection
import com.rangerscards.data.local.card.FullCardProjection
import com.rangerscards.data.local.dao.CampaignDao
import com.rangerscards.data.local.deck.DeckListItemProjection
import com.rangerscards.data.local.deck.RoleCardProjection
import com.rangerscards.domain.repository.CampaignsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonElement

class OfflineCampaignRepository(
    private val campaignDao: CampaignDao,
) : CampaignsRepository {

    override suspend fun updateCampaign(campaign: Campaign) = campaignDao.updateCampaign(campaign)

    override suspend fun insertCampaign(campaign: Campaign) = campaignDao.insertCampaign(campaign)

    override suspend fun upsertChallengeDeck(campaignId: String, challengeDeckIds: JsonElement) =
        campaignDao.upsertChallengeDeck(ChallengeDeck(campaignId, challengeDeckIds))

    override fun getCampaignFlowById(id: String): Flow<Campaign?> = campaignDao.getCampaignFlowById(id)

    override suspend fun getCampaignById(id: String): Campaign = campaignDao.getCampaignById(id)

    override fun getCampaignChallengeDeckFlowById(campaignId: String): Flow<JsonElement?> =
        campaignDao.getCampaignChallengeDeckFlowById(campaignId)

    override fun getRole(id: String, taboo: Boolean): Flow<RoleCardProjection?> = campaignDao.getRole(id, taboo)

    override fun getAllDecks(userId: String, uploaded: Boolean): Flow<PagingData<DeckListItemProjection>> {
        // Create a Pager that wraps the PagingSource from the DAO.
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false,
                initialLoadSize = 20
            ),
            pagingSourceFactory = { campaignDao.getAllDecks(userId, uploaded) }
        ).flow
    }

    override fun searchDecks(query: String, userId: String, uploaded: Boolean): Flow<PagingData<DeckListItemProjection>> {
        val newQuery = query
            .lowercase()
            .replace("\"(\\[\"]|.*)?\"".toRegex(), " ")
            .split("[^\\p{Alnum}]+".toRegex())
            .filter { it.isNotBlank() }
            .joinToString(separator = " ", transform = { "%$it%" })
        // Create a Pager that wraps the PagingSource from the DAO.
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false,
                initialLoadSize = 20
            ),
            pagingSourceFactory = { campaignDao.searchDecks(newQuery, userId, uploaded) }
        ).flow
    }

    override suspend fun deleteCampaign(id: String) = campaignDao.deleteCampaign(id)

    override fun getRewards(taboo: Boolean, packIds: List<String>): Flow<List<CardListItemProjection>> = campaignDao.getAllRewards(taboo, packIds)

    override fun getCardById(cardCode: String, taboo: Boolean): Flow<FullCardProjection> =
        campaignDao.getCardById(cardCode, taboo)
}