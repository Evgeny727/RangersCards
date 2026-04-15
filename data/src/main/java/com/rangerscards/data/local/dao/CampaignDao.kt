package com.rangerscards.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.rangerscards.data.local.campaign.Campaign
import com.rangerscards.data.local.campaign.CampaignListItemProjection
import com.rangerscards.data.local.campaign.ChallengeDeck
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonElement

@Dao
interface CampaignDao {

    @Upsert
    suspend fun upsertCampaign(campaign: Campaign)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateCampaign(campaign: Campaign)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampaign(campaign: Campaign)

    @Query("DELETE FROM campaign WHERE id = :id")
    suspend fun deleteCampaignById(id: String)

    @Upsert
    suspend fun upsertAllCampaigns(campaigns: List<Campaign>)

    @Upsert
    suspend fun upsertChallengeDeck(challengeDeck: ChallengeDeck)

    @Query("DELETE FROM campaign WHERE id NOT IN (:ids) AND uploaded = 1")
    suspend fun deleteNotIn(ids: List<String>)

    @Query("DELETE FROM campaign WHERE uploaded = 1")
    suspend fun deleteAllUploadedCampaigns()

    @Query("DELETE FROM campaign WHERE uploaded = 0")
    suspend fun deleteAllLocalCampaigns()

    @Query("SELECT id, cycle_id, name, day, current_location, latest_decks, access FROM campaign " +
            "WHERE cycle_id != 'demo' AND next_campaign_id IS NULL ORDER BY updated_at DESC"
    )
    fun getAllCampaigns(): PagingSource<Int, CampaignListItemProjection>

    @Query("SELECT id, cycle_id, name, day, current_location, latest_decks, access FROM campaign " +
            "WHERE name LIKE :query AND cycle_id != 'demo' AND next_campaign_id IS NULL ORDER BY updated_at DESC"
    )
    fun searchCampaigns(query: String): PagingSource<Int, CampaignListItemProjection>

    @Query("SELECT id, cycle_id, name, day, current_location, latest_decks, access FROM campaign " +
            "WHERE (user_id == :userId OR user_id == '') AND cycle_id != 'demo' AND cycle_id != :cycleId AND next_campaign_id IS NULL ORDER BY updated_at DESC"
    )
    fun getAllCampaignsForTransfer(cycleId: String, userId: String): PagingSource<Int, CampaignListItemProjection>

    @Transaction
    suspend fun syncCampaigns(networkData: List<Campaign>) {
        // Insert or update all the network data.
        upsertAllCampaigns(networkData)

        if (networkData.isEmpty()) {
            // If the network data is empty, clear the rows with uploaded = true.
            deleteAllUploadedCampaigns()
        } else {
            // Otherwise, delete any rows not present in the network data.
            val networkIds = networkData.map { it.id }
            deleteNotIn(networkIds)
        }
    }

    @Query("SELECT * FROM campaign WHERE id = :id")
    fun getCampaignFlowById(id: String): Flow<Campaign>

    @Query("SELECT challenge_deck_ids FROM challenge_deck WHERE id = :id")
    fun getCampaignChallengeDeckFlowById(id: String): Flow<JsonElement?>

    @Query("SELECT * FROM campaign WHERE id = :id")
    suspend fun getCampaignById(id: String): Campaign?
}