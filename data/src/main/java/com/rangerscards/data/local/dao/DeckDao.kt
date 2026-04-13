package com.rangerscards.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.rangerscards.data.local.deck.Deck
import com.rangerscards.data.local.deck.DeckListItemProjection

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

    @Update
    suspend fun updateAllDecks(decks: List<Deck>)

    @Upsert
    suspend fun upsertAllDecks(decks: List<Deck>)

    @Query("DELETE FROM deck WHERE id NOT IN (:ids) AND uploaded = 1")
    suspend fun deleteNotIn(ids: List<String>)

    @Query("DELETE FROM deck WHERE uploaded = 1")
    suspend fun deleteAllUploadedDecks()

    @Query("DELETE FROM deck WHERE uploaded = 0")
    suspend fun deleteAllLocalDecks()

    @Query("SELECT id, user_handle, name, meta, campaign_name FROM deck WHERE next_id IS NULL " +
            "AND (uploaded = :uploaded OR :uploaded is NULL) AND (user_id = :userId OR user_id = '') ORDER BY updated_at DESC"
    )
    fun getAllDecks(userId: String, uploaded: Boolean? = null): PagingSource<Int, DeckListItemProjection>

    @Query("SELECT id, user_handle, name, meta, campaign_name FROM deck WHERE next_id IS NULL " +
            " AND (uploaded = :uploaded OR :uploaded is NULL) AND name LIKE :query " +
            "AND (user_id = :userId OR user_id = '') ORDER BY updated_at DESC"
    )
    fun searchDecks(query: String, userId: String, uploaded: Boolean? = null): PagingSource<Int, DeckListItemProjection>

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