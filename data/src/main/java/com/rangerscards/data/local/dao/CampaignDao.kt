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
import com.rangerscards.data.local.card.CardListItemProjection
import com.rangerscards.data.local.card.FullCardProjection
import com.rangerscards.data.local.deck.DeckListItemProjection
import com.rangerscards.data.local.deck.RoleCardProjection
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonElement

@Dao
interface CampaignDao {

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
    fun getAllCampaignsForTransfer(cycleId: String, userId: String): Flow<List<CampaignListItemProjection>>

    @Query("Select id, name, text, real_image_src, traits, taboo_id FROM card WHERE id IN (:ids)")
    fun getRolesImages(ids: List<String>): Flow<List<RoleCardProjection>>

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
    fun getCampaignFlowById(id: String): Flow<Campaign?>

    @Query("SELECT challenge_deck_ids FROM challenge_deck WHERE id = :id")
    fun getCampaignChallengeDeckFlowById(id: String): Flow<JsonElement?>

    @Query("SELECT * FROM campaign WHERE id = :id")
    suspend fun getCampaignById(id: String): Campaign

    @Query(
        """SELECT * FROM (
            -- Case 1: Taboo is set – select the override card
            SELECT id, name, text, real_image_src, traits, taboo_id
            FROM card WHERE code = :code AND (:taboo IS 1 AND taboo_id IS NOT NULL)
            UNION ALL
            -- Case 2: When taboo is set but no override exists, fall back to the default card
            SELECT id, name, text, real_image_src, traits, taboo_id
            FROM card AS c WHERE c.code = :code AND (:taboo IS 1 AND taboo_id IS NULL)
              AND NOT EXISTS ( SELECT 1 FROM card c2 WHERE c2.code = c.code AND c2.taboo_id IS NOT NULL)
            UNION ALL
            -- Case 3: Taboo not set – select only default cards
            SELECT id, name, text, real_image_src, traits, taboo_id
            FROM card WHERE code = :code AND (:taboo IS 0 AND taboo_id IS NULL)
        )"""
    )
    fun getRole(code: String, taboo: Boolean): Flow<RoleCardProjection?>

    @Query("SELECT id, user_handle, name, meta, campaign_name FROM deck WHERE next_id IS NULL AND uploaded = :uploaded " +
            " AND campaign_id IS NULL AND (user_id = :userId OR user_id = '') ORDER BY updated_at DESC"
    )
    fun getAllDecks(userId: String, uploaded: Boolean): PagingSource<Int, DeckListItemProjection>

    @Query("SELECT id, user_handle, name, meta, campaign_name FROM deck WHERE next_id IS NULL AND uploaded = :uploaded " +
            " AND campaign_id IS NULL AND name LIKE :query AND (user_id = :userId OR user_id = '') ORDER BY updated_at DESC"
    )
    fun searchDecks(query: String, userId: String, uploaded: Boolean): PagingSource<Int, DeckListItemProjection>

    @Query("DELETE FROM campaign WHERE id = :id")
    suspend fun deleteCampaign(id: String)

    @Query("""SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name, equip,
            type_name, traits, level, approach_connection, approach_reason, approach_conflict, approach_exploration FROM (
            -- Case 1: When a taboo is set, get the taboo-specific card for each code that exists.
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name,
            type_name, traits, real_traits, level, set_id, set_type_id, set_position, deck_limit, equip,
            approach_connection, approach_reason, approach_conflict, approach_exploration
            FROM card WHERE pack_id IN (:packIds) AND set_id == 'reward' AND (:taboo IS 1 AND taboo_id IS NOT NULL)
            UNION ALL
            -- Case 2: When a taboo is set but no override exists, fall back to the default card.
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name,
            type_name, traits, real_traits, level, set_id, set_type_id, set_position, deck_limit, equip,
            approach_connection, approach_reason, approach_conflict, approach_exploration
            FROM card WHERE pack_id IN (:packIds) AND set_id == 'reward' AND (:taboo IS 1 AND taboo_id IS NULL) 
            AND NOT EXISTS (SELECT 1 FROM card t WHERE t.code = card.code AND t.taboo_id IS NOT NULL)
            UNION ALL
            -- Case 3: When no taboo is set, simply return the default card.
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name,
            type_name, traits, real_traits, level, set_id, set_type_id, set_position, deck_limit, equip,
            approach_connection, approach_reason, approach_conflict, approach_exploration
            FROM card WHERE pack_id IN (:packIds) AND set_id == 'reward' AND (:taboo IS 0 AND taboo_id IS NULL)
        ) ORDER BY (set_type_id IS NULL), set_type_id, set_id, set_position""")
    fun getAllRewards(taboo: Boolean, packIds: List<String>): Flow<List<CardListItemProjection>>

    @Query("""SELECT * FROM (
            -- Case 1: Taboo is set – choose the taboo-specific card
            SELECT taboo_id, aspect_id, aspect_short_name, cost, image_src, real_image_src, name, type_id,
                presence, approach_conflict, approach_reason, approach_exploration, approach_connection,
                type_name, traits, equip, harm, progress, token_plurals, token_count, text, flavor, level,
                set_name, set_size, set_position, pack_short_name, subset_name, subset_position, subset_size,
                sun_challenge, mountain_challenge, crest_challenge
            FROM card WHERE code = :cardCode AND (:taboo IS 1 AND taboo_id IS NOT NULL)
            UNION ALL
            -- Case 2: When taboo is set but no override exists, return the default card
            SELECT taboo_id, aspect_id, aspect_short_name, cost, image_src, real_image_src, name, type_id,
                presence, approach_conflict, approach_reason, approach_exploration, approach_connection,
                type_name, traits, equip, harm, progress, token_plurals, token_count, text, flavor, level,
                set_name, set_size, set_position, pack_short_name, subset_name, subset_position, subset_size,
                sun_challenge, mountain_challenge, crest_challenge
            FROM card AS c WHERE code = :cardCode AND (:taboo IS 1 AND taboo_id IS NULL)
              AND NOT EXISTS ( SELECT 1 FROM card c2 WHERE c2.code = c.code AND c2.taboo_id IS NOT NULL)
            UNION ALL
            -- Case 3: Taboo not set – return default card
            SELECT taboo_id, aspect_id, aspect_short_name, cost, image_src, real_image_src, name, type_id,
                presence, approach_conflict, approach_reason, approach_exploration, approach_connection,
                type_name, traits, equip, harm, progress, token_plurals, token_count, text, flavor, level,
                set_name, set_size, set_position, pack_short_name, subset_name, subset_position, subset_size,
                sun_challenge, mountain_challenge, crest_challenge
            FROM card WHERE code = :cardCode AND (:taboo IS 0 AND taboo_id IS NULL)
        )""")
    fun getCardById(cardCode: String, taboo: Boolean): Flow<FullCardProjection>
}