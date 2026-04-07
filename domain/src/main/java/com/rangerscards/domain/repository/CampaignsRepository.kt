package com.rangerscards.domain.repository

import androidx.paging.PagingData
import com.rangerscards.domain.model.Campaign
import com.rangerscards.domain.model.CampaignListItem
import kotlinx.coroutines.flow.Flow

interface CampaignsRepository {

    suspend fun deleteAllUploadedCampaigns()

    suspend fun deleteAllLocalCampaigns()

    suspend fun syncCampaigns(userId: String)

    suspend fun syncCampaignById(id: String)

    fun getAllPaginatedCampaignsFlow(): Flow<PagingData<CampaignListItem>>

    fun searchPaginatedCampaignsFlow(query: String): Flow<PagingData<CampaignListItem>>

    fun getAllCampaignsForTransferFlow(cycleId: String, userId: String): Flow<List<CampaignListItem>>

    suspend fun createCampaign(campaign: Campaign, transferCampaignId: String? = null)

    fun getCampaignFlowById(id: String): Flow<Campaign?>

    suspend fun getCampaignById(id: String): Result<Campaign>

    suspend fun uploadCampaign(campaign: Campaign)

    suspend fun updateCampaign(campaign: Campaign, remoteUpdateAction: RemoteUpdateAction)

    suspend fun startCampaignSubscription(campaignId: String): Result<Unit>

    suspend fun addFriendToCampaign(campaignId: String, friendUserId: String)

    suspend fun removeFriendToCampaign(campaignId: String, friendUserId: String)

    suspend fun leaveCampaign(campaignId: String, userId: String)

    suspend fun deleteCampaignById(id: String, uploaded: Boolean)

    suspend fun upsertChallengeDeck(campaignId: String, challengeDeckIds: List<String>)

    fun getCampaignChallengeDeckFlowById(campaignId: String): Flow<List<String>?>
}

enum class RemoteUpdateAction {
    SET_EXPANSIONS, SET_TITLE, SET_REWARDS, ADD_EVENT, SET_EVENTS, SET_REMOVED, ADD_REMOVED,
    EXTEND, ADD_MISSION, SET_MISSION, SET_CALENDAR, SET_DAY, UNDO_TRAVEL, TRAVEL
}