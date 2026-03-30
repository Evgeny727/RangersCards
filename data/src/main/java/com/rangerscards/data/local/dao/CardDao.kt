package com.rangerscards.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Upsert
import androidx.sqlite.db.SupportSQLiteQuery
import com.rangerscards.data.local.card.Card
import com.rangerscards.data.local.card.CardFts
import com.rangerscards.data.local.card.CardListItemProjection
import com.rangerscards.data.local.card.FullCardProjection
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cards: List<Card>)

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
    fun getCardById(cardCode: String, taboo: Boolean): Flow<FullCardProjection?>
}