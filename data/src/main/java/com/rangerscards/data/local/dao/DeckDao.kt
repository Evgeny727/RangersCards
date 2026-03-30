package com.rangerscards.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import androidx.sqlite.db.SupportSQLiteQuery
import com.rangerscards.data.database.card.Card
import com.rangerscards.data.database.card.CardDeckListItemProjection
import com.rangerscards.data.database.card.CardFts
import com.rangerscards.data.database.card.CardListItemProjection
import com.rangerscards.data.database.deck.Deck
import com.rangerscards.data.database.deck.DeckListItemProjection
import com.rangerscards.data.database.deck.RoleCardProjection
import kotlinx.coroutines.flow.Flow

@Dao
interface DeckDao {

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateDeck(deck: Deck)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeck(deck: Deck)

    @Upsert
    suspend fun upsertDeck(deck: Deck)

    @Query("DELETE FROM deck WHERE id = :id")
    suspend fun deleteDeckById(id: String)

    @Query("DELETE FROM deck WHERE id IN (:ids)")
    suspend fun deleteDecksById(ids: List<String>)

    @Upsert
    suspend fun upsertAllDecks(decks: List<Deck>)

    @Query("DELETE FROM deck WHERE id NOT IN (:ids) AND uploaded = 1")
    suspend fun deleteNotIn(ids: List<String>)

    @Query("DELETE FROM deck WHERE uploaded = 1")
    suspend fun deleteAllUploadedDecks()

    @Query("DELETE FROM deck WHERE uploaded = 0")
    suspend fun deleteAllLocalDecks()

    @Query("SELECT id, user_handle, name, meta, campaign_name FROM deck WHERE next_id IS NULL " +
            "AND (user_id = :userId OR user_id = '') ORDER BY updated_at DESC"
    )
    fun getAllDecks(userId: String): PagingSource<Int, DeckListItemProjection>

    @Query("SELECT id, user_handle, name, meta, campaign_name FROM deck WHERE next_id IS NULL " +
            "AND name LIKE :query AND (user_id = :userId OR user_id = '') ORDER BY updated_at DESC"
    )
    fun searchDecks(query: String, userId: String): PagingSource<Int, DeckListItemProjection>

    @Query("Select id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name, " +
            "type_name, traits, level, equip, approach_conflict, approach_reason, approach_exploration, " +
            "approach_connection FROM card WHERE id = :id")
    fun getCard(id: String): Flow<CardListItemProjection?>

    @Query("""SELECT * FROM (
            -- Case 1: Taboo is set – select the override card
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name, equip,
            type_name, traits, level, approach_connection, approach_reason, approach_conflict, approach_exploration 
            FROM card WHERE type_id = 'role' AND set_id = :specialty AND (:taboo IS 1 AND taboo_id IS NOT NULL)
            AND pack_id IN (:packIds)
            UNION ALL
            -- Case 2: When taboo is set but no override exists, fall back to the default card
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name, equip,
            type_name, traits, level, approach_connection, approach_reason, approach_conflict, approach_exploration
            FROM card AS c WHERE c.type_id = 'role' AND c.set_id = :specialty AND (:taboo IS 1 AND taboo_id IS NULL)
            AND pack_id IN (:packIds)
            AND NOT EXISTS ( SELECT 1 FROM card c2 WHERE c2.code = c.code AND c2.taboo_id IS NOT NULL)
            UNION ALL
            -- Case 3: Taboo not set – select only default cards
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name, equip,
            type_name, traits, level, approach_connection, approach_reason, approach_conflict, approach_exploration 
            FROM card WHERE type_id = 'role' AND set_id = :specialty AND (:taboo IS 0 AND taboo_id IS NULL)
            AND pack_id IN (:packIds)
        )""")
    fun getRoles(specialty: String, taboo: Boolean, packIds: List<String>): PagingSource<Int, CardListItemProjection>

    @Transaction
    suspend fun syncDecks(networkData: List<Deck>) {
        // Insert or update all the network data.
        upsertAllDecks(networkData)

        if (networkData.isEmpty()) {
            // If the network data is empty, clear the rows with uploaded = true.
            deleteAllUploadedDecks()
        } else {
            // Otherwise, delete any rows not present in the network data.
            val networkIds = networkData.map { it.id }
            deleteNotIn(networkIds)
        }
    }

    @Query("SELECT * FROM deck WHERE id = :id")
    suspend fun getDeckById(id: String): Deck?

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
    suspend fun getRole(code: String, taboo: Boolean): RoleCardProjection?

    @Query("""SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name, 
                   type_name, traits, real_traits, level, set_id, set_type_id, deck_limit, equip,
                   approach_connection, approach_reason, approach_conflict, approach_exploration FROM (
            -- Case 1: When a taboo is set, get the taboo-specific card for each code that exists.
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name, 
                   type_name, traits, real_traits, level, set_id, set_type_id, set_position, deck_limit, 
                   approach_connection, approach_reason, approach_conflict, approach_exploration, equip
            FROM card WHERE code IN (:ids) AND (:tabooId IS NOT NULL) AND taboo_id = :tabooId
            UNION ALL
            -- Case 2: When a taboo is set but no override exists, fall back to the default card.
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name, 
                   type_name, traits, real_traits, level, set_id, set_type_id, set_position, deck_limit, 
                   approach_connection, approach_reason, approach_conflict, approach_exploration, equip
            FROM card WHERE code IN (:ids) AND taboo_id IS NULL AND (:tabooId IS NOT NULL) 
            AND NOT EXISTS (SELECT 1 FROM card t WHERE t.code = card.code AND t.taboo_id = :tabooId)
            UNION ALL
            -- Case 3: When no taboo is set, simply return the default card.
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name, 
                   type_name, traits, real_traits, level, set_id, set_type_id, set_position, deck_limit, 
                   approach_connection, approach_reason, approach_conflict, approach_exploration, equip
            FROM card WHERE code IN (:ids) AND (:tabooId IS NULL) AND taboo_id IS NULL
        ) ORDER BY set_type_id, set_id, set_position""")
    fun getCardsByIds(ids: List<String>, tabooId: String?): Flow<List<CardDeckListItemProjection>>

    @Query("""SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name, 
                   type_name, traits, real_traits, level, set_id, set_type_id, deck_limit, equip,
                   approach_connection, approach_reason, approach_conflict, approach_exploration FROM (
            -- Case 1: When a taboo is set, get the taboo-specific card for each code that exists.
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name, 
                   type_name, traits, real_traits, level, set_id, set_type_id, set_position, deck_limit, 
                   approach_connection, approach_reason, approach_conflict, approach_exploration, equip
            FROM card WHERE code IN (:ids) AND (:tabooId IS NOT NULL) AND taboo_id = :tabooId
            UNION ALL
            -- Case 2: When a taboo is set but no override exists, fall back to the default card.
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name, 
                   type_name, traits, real_traits, level, set_id, set_type_id, set_position, deck_limit, 
                   approach_connection, approach_reason, approach_conflict, approach_exploration, equip
            FROM card WHERE code IN (:ids) AND taboo_id IS NULL AND (:tabooId IS NOT NULL) 
            AND NOT EXISTS (SELECT 1 FROM card t WHERE t.code = card.code AND t.taboo_id = :tabooId)
            UNION ALL
            -- Case 3: When no taboo is set, simply return the default card.
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name, 
                   type_name, traits, real_traits, level, set_id, set_type_id, set_position, deck_limit, 
                   approach_connection, approach_reason, approach_conflict, approach_exploration, equip
            FROM card WHERE code IN (:ids) AND (:tabooId IS NULL) AND taboo_id IS NULL
        ) ORDER BY set_type_id, set_id, set_position""")
    suspend fun getChangedCardsByIds(ids: List<String>, tabooId: String?): List<CardDeckListItemProjection>

    @RawQuery(observedEntities = [Card::class, CardFts::class])
    fun searchCardsRaw(query: SupportSQLiteQuery): PagingSource<Int, CardDeckListItemProjection>

    @Query("""
    WITH RECURSIVE
      prevs(id, depth) AS (
        SELECT previous_id, 1 FROM deck WHERE id = :startId AND previous_id IS NOT NULL
        UNION ALL
        SELECT d.previous_id, depth + 1
        FROM deck d
        JOIN prevs p ON d.id = p.id
        WHERE d.previous_id IS NOT NULL
      ),
      nexts(id, depth) AS (
        SELECT next_id, 1 FROM deck WHERE id = :startId AND next_id IS NOT NULL
        UNION ALL
        SELECT d.next_id, depth + 1
        FROM deck d
        JOIN nexts n ON d.id = n.id
        WHERE d.next_id IS NOT NULL
      )
    SELECT id FROM (
      SELECT id, -depth AS ord FROM prevs
      UNION ALL
      SELECT :startId AS id, 0 AS ord
      UNION ALL
      SELECT id, depth AS ord FROM nexts
    )
    ORDER BY ord DESC
  """)
    suspend fun getAllVersionDeckIds(startId: String): List<String>
}