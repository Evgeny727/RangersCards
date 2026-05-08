package com.rangerscards.domain.repository

import androidx.paging.PagingData
import com.rangerscards.domain.model.Campaign
import com.rangerscards.domain.model.CampaignListItem
import kotlinx.coroutines.flow.Flow

interface CampaignsRepository {

    suspend fun deleteAllLocalCampaigns()

    suspend fun syncCampaigns(userId: String): Result<Unit>

    fun getAllPaginatedCampaignsFlow(): Flow<PagingData<CampaignListItem>>

    fun searchPaginatedCampaignsFlow(query: String): Flow<PagingData<CampaignListItem>>

    fun getAllPaginatedCampaignsForTransferFlow(cycleId: String, userId: String): Flow<PagingData<CampaignListItem>>

    fun getCampaignFlowById(id: String): Flow<Campaign>

    suspend fun createCampaign(
        uploaded: Boolean,
        name: String,
        cycleId: String,
        currentLocation: String,
        expansions: List<String>,
        transferCampaignId: String? = null
    ): Result<String>

    suspend fun uploadCampaign(campaign: Campaign): Result<String>

    suspend fun updateCampaign(campaign: Campaign, remoteUpdateAction: RemoteUpdateAction): Result<Unit>

    suspend fun addFriendToCampaign(campaignId: String, friendUserId: String): Result<Unit>

    suspend fun removeFriendFromCampaign(campaignId: String, friendUserId: String): Result<Unit>

    suspend fun leaveCampaign(campaignId: String, userId: String): Result<Unit>

    suspend fun deleteCampaignById(id: String, uploaded: Boolean): Result<String?>

    suspend fun upsertChallengeDeck(campaignId: String, challengeDeckIds: List<Int>)

    fun getCampaignChallengeDeckFlowById(campaignId: String): Flow<List<Int>>
    fun startSubscription(campaignId: String): Flow<Result<Unit>>
}

enum class RemoteUpdateAction {
    SET_EXPANSIONS, SET_TITLE, SET_NOTES, SET_REWARDS, SET_EVENTS, SET_REMOVED,
    EXTEND, SET_MISSION, SET_CALENDAR, SET_DAY, SET_TRAVEL
}