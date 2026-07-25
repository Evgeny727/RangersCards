package com.rangerscards.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Upsert
import androidx.sqlite.db.SupportSQLiteQuery
import com.rangerscards.data.local.card.Card
import com.rangerscards.data.local.card.CardDeckListItemProjection
import com.rangerscards.data.local.card.CardDeckMulliganProjection
import com.rangerscards.data.local.card.CardFts
import com.rangerscards.data.local.card.CardListItemProjection
import com.rangerscards.data.local.card.FullCardProjection
import com.rangerscards.data.local.deck.RoleCardProjection
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {

    @Upsert
    suspend fun upsertAll(cards: List<Card>)

    @Query("SELECT EXISTS(SELECT * FROM card)")
    suspend fun isExists(): Boolean

    @RawQuery(observedEntities = [Card::class, CardFts::class])
    fun searchCardsRaw(query: SupportSQLiteQuery): PagingSource<Int, CardListItemProjection>

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
    fun getCardByCodeFlow(cardCode: String, taboo: Boolean): Flow<FullCardProjection?>

    @Query("""SELECT * FROM (
            -- Case 1: Taboo is set – select the override card
            SELECT id, code, name, text, real_image_src, taboo_id FROM card WHERE type_id = 'role' 
            AND set_id = :specialty AND (:taboo IS 1 AND taboo_id IS NOT NULL) AND pack_id IN (:packIds)
            UNION ALL
            -- Case 2: When taboo is set but no override exists, fall back to the default card
            SELECT id, code, name, text, real_image_src, taboo_id FROM card AS c WHERE c.type_id = 'role' 
            AND c.set_id = :specialty AND (:taboo IS 1 AND taboo_id IS NULL) AND pack_id IN (:packIds)
            AND NOT EXISTS ( SELECT 1 FROM card c2 WHERE c2.code = c.code AND c2.taboo_id IS NOT NULL)
            UNION ALL
            -- Case 3: Taboo not set – select only default cards
            SELECT id, code, name, text, real_image_src, taboo_id FROM card WHERE type_id = 'role' 
            AND set_id = :specialty AND (:taboo IS 0 AND taboo_id IS NULL) AND pack_id IN (:packIds)
        )""")
    fun getPaginatedRoles(specialty: String, taboo: Boolean, packIds: List<String>): PagingSource<Int, RoleCardProjection>

    @Query(
        """SELECT * FROM (
            -- Case 1: Taboo is set – select the override card
            SELECT id, code, name, text, real_image_src, taboo_id
            FROM card WHERE code = :code AND (:taboo IS 1 AND taboo_id IS NOT NULL)
            UNION ALL
            -- Case 2: When taboo is set but no override exists, fall back to the default card
            SELECT id, code, name, text, real_image_src, taboo_id
            FROM card AS c WHERE c.code = :code AND (:taboo IS 1 AND taboo_id IS NULL)
              AND NOT EXISTS ( SELECT 1 FROM card c2 WHERE c2.code = c.code AND c2.taboo_id IS NOT NULL)
            UNION ALL
            -- Case 3: Taboo not set – select only default cards
            SELECT id, code, name, text, real_image_src, taboo_id
            FROM card WHERE code = :code AND (:taboo IS 0 AND taboo_id IS NULL)
        )"""
    )
    fun getRoleByCode(code: String, taboo: Boolean): Flow<RoleCardProjection?>

    @Query(
        """SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name, 
                   type_name, traits, real_traits, level, set_id, set_type_id, deck_limit, equip,
                   approach_connection, approach_reason, approach_conflict, approach_exploration FROM (
            -- Case 1: When a taboo is set, get the taboo-specific card for each code that exists.
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name, 
                   type_name, traits, real_traits, level, set_id, set_type_id, set_position, deck_limit, 
                   approach_connection, approach_reason, approach_conflict, approach_exploration, equip
            FROM card WHERE code IN (:codes) AND (:tabooId IS NOT NULL) AND taboo_id = :tabooId
            UNION ALL
            -- Case 2: When a taboo is set but no override exists, fall back to the default card.
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name, 
                   type_name, traits, real_traits, level, set_id, set_type_id, set_position, deck_limit, 
                   approach_connection, approach_reason, approach_conflict, approach_exploration, equip
            FROM card WHERE code IN (:codes) AND taboo_id IS NULL AND (:tabooId IS NOT NULL) 
            AND NOT EXISTS (SELECT 1 FROM card t WHERE t.code = card.code AND t.taboo_id = :tabooId)
            UNION ALL
            -- Case 3: When no taboo is set, simply return the default card.
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name, 
                   type_name, traits, real_traits, level, set_id, set_type_id, set_position, deck_limit, 
                   approach_connection, approach_reason, approach_conflict, approach_exploration, equip
            FROM card WHERE code IN (:codes) AND (:tabooId IS NULL) AND taboo_id IS NULL
        ) ORDER BY set_type_id, set_id, set_position"""
    )
    fun getCardsByCodes(codes: List<String>, tabooId: String?): Flow<List<CardDeckListItemProjection>>

    @Query(
        """SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name, 
                   type_name, traits, real_traits, level, set_id, set_type_id, deck_limit, equip,
                   approach_connection, approach_reason, approach_conflict, approach_exploration FROM (
            -- Case 1: When a taboo is set, get the taboo-specific card for each code that exists.
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name, 
                   type_name, traits, real_traits, level, set_id, set_type_id, set_position, deck_limit, 
                   approach_connection, approach_reason, approach_conflict, approach_exploration, equip
            FROM card WHERE code IN (:codes) AND (:tabooId IS NOT NULL) AND taboo_id = :tabooId
            UNION ALL
            -- Case 2: When a taboo is set but no override exists, fall back to the default card.
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name, 
                   type_name, traits, real_traits, level, set_id, set_type_id, set_position, deck_limit, 
                   approach_connection, approach_reason, approach_conflict, approach_exploration, equip
            FROM card WHERE code IN (:codes) AND taboo_id IS NULL AND (:tabooId IS NOT NULL) 
            AND NOT EXISTS (SELECT 1 FROM card t WHERE t.code = card.code AND t.taboo_id = :tabooId)
            UNION ALL
            -- Case 3: When no taboo is set, simply return the default card.
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name, 
                   type_name, traits, real_traits, level, set_id, set_type_id, set_position, deck_limit, 
                   approach_connection, approach_reason, approach_conflict, approach_exploration, equip
            FROM card WHERE code IN (:codes) AND (:tabooId IS NULL) AND taboo_id IS NULL
        ) ORDER BY set_type_id, set_id, set_position"""
    )
    suspend fun getChangedCardsByCodes(codes: List<String>, tabooId: String?): List<CardDeckListItemProjection>

    @RawQuery(observedEntities = [Card::class, CardFts::class])
    fun searchDeckCardsRaw(query: SupportSQLiteQuery): PagingSource<Int, CardDeckListItemProjection>

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

    @Query("Select id, code, name, text, real_image_src, taboo_id FROM card WHERE id IN (:ids)")
    fun getRolesImages(ids: List<String>): Flow<List<RoleCardProjection>>

    @Query("Select id, code, taboo_id, name, approach_conflict, approach_reason, " +
            "approach_exploration, approach_connection, traits, level, type_name, setup, " +
            "cost, aspect_id, aspect_short_name, image_src, real_image_src FROM card WHERE id IN (:ids)")
    suspend fun getMulliganCardsByIds(ids: List<String>): List<CardDeckMulliganProjection>
}