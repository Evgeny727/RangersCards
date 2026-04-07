package com.rangerscards.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.sqlite.db.SimpleSQLiteQuery
import com.rangerscards.objects.CardFilterOptions
import com.rangerscards.objects.CardFilters
import com.rangerscards.data.local.card.CardDeckListItemProjection
import com.rangerscards.data.local.dao.DeckDao
import com.rangerscards.data.local.deck.Deck
import com.rangerscards.data.local.deck.RoleCardProjection
import com.rangerscards.data.objects.PorterStem
import com.rangerscards.ui.deck.DeckInfo
import kotlinx.coroutines.flow.Flow
import java.util.Locale

class OfflineDeckRepository(private val deckDao: DeckDao) : DeckRepository {

    override suspend fun getDeck(id: String): Deck? = deckDao.getDeckById(id)

    override suspend fun getRole(code: String, taboo: Boolean): RoleCardProjection? =
        deckDao.getRole(code, taboo)

    override suspend fun updateDeck(deck: Deck) = deckDao.updateDeck(deck)

    override suspend fun insertDeck(deck: Deck) = deckDao.insertDeck(deck)

    override suspend fun upsertDeck(deck: Deck) = deckDao.upsertDeck(deck)

    override suspend fun upsertDecks(decks: List<Deck>) = deckDao.upsertAllDecks(decks)

    override suspend fun deleteDeckById(id: String) = deckDao.deleteDeckById(id)

    override suspend fun deleteDecksById(ids: List<String>) = deckDao.deleteDecksById(ids)

    override fun getCardsByIds(ids: List<String>, tabooId: String?): Flow<List<CardDeckListItemProjection>> =
        deckDao.getCardsByIds(ids, tabooId)

    override suspend fun getChangedCardsByIds(ids: List<String>, tabooId: String?): List<CardDeckListItemProjection> =
        deckDao.getChangedCardsByIds(ids, tabooId)

    override fun getAllCards(
        deckInfo: DeckInfo,
        typeIndex: Int,
        showAllSpoilers: Boolean,
        packIds: List<String>,
        filterOptions: CardFilterOptions
    ): Flow<PagingData<CardDeckListItemProjection>> {
        val rawQuery = if (!deckInfo.isUpgrade) {
            when(typeIndex) {
                0 -> buildSearchCardsQuery(
                    additionalClause = "set_id = 'personality'",
                    orderByClause = "aspect_id, set_position",
                    taboo = deckInfo.taboo,
                    isPacksNeeded = true,
                    packIds = packIds,
                    filterOptions = filterOptions
                )
                1 -> buildSearchCardsQuery(
                    additionalClause = "set_id = ? AND set_type_id = 'background' AND type_id != 'role'",
                    orderByClause = "aspect_id, set_position",
                    background = deckInfo.background,
                    taboo = deckInfo.taboo,
                    isPacksNeeded = true,
                    packIds = packIds,
                    filterOptions = filterOptions
                )
                2 -> buildSearchCardsQuery(
                    additionalClause = "set_id = ? AND set_type_id = 'specialty' AND type_id != 'role'",
                    orderByClause = "aspect_id, set_position",
                    specialty = deckInfo.specialty,
                    taboo = deckInfo.taboo,
                    isPacksNeeded = true,
                    packIds = packIds,
                    filterOptions = filterOptions
                )
                else -> buildSearchCardsQuery(
                    additionalClause = "set_id != ? AND set_id != ? AND type_id != 'role' AND set_id != 'personality' AND real_traits NOT LIKE '%expert%'",
                    orderByClause = "(set_type_id IS NULL), set_type_id, set_id, set_position",
                    background = deckInfo.background,
                    specialty = deckInfo.specialty,
                    taboo = deckInfo.taboo,
                    isPacksNeeded = true,
                    packIds = packIds,
                    filterOptions = filterOptions
                )
            }
        } else {
            when(typeIndex) {
                0 -> if (showAllSpoilers) buildSearchCardsQuery(
                    additionalClause = "set_id == 'reward'",
                    orderByClause = "(set_type_id IS NULL), set_type_id, set_id, set_position",
                    taboo = deckInfo.taboo,
                    isPacksNeeded = true,
                    packIds = packIds,
                    filterOptions = filterOptions
                ) else buildSearchCardsQuery(
                    additionalClause = "code IN (${deckInfo.rewards.joinToString { "?" }})",
                    orderByClause = "(set_type_id IS NULL), set_type_id, set_id, set_position",
                    rewards = deckInfo.rewards,
                    taboo = deckInfo.taboo,
                    filterOptions = filterOptions
                )
                1 -> buildSearchCardsQuery(
                    additionalClause = "set_id == 'malady'",
                    orderByClause = "(set_type_id IS NULL), set_type_id, set_id, set_position",
                    taboo = deckInfo.taboo,
                    isPacksNeeded = true,
                    packIds = packIds,
                    filterOptions = filterOptions
                )
                2 -> buildSearchCardsQuery(
                    additionalClause = "spoiler = 'false' OR (spoiler IS NULL AND NOT EXISTS (SELECT 1 FROM card WHERE spoiler = 'false')) AND type_id != 'role'",
                    orderByClause = "(set_type_id IS NULL), set_type_id, set_id, set_position",
                    taboo = deckInfo.taboo,
                    isPacksNeeded = true,
                    packIds = packIds,
                    filterOptions = filterOptions
                )
                else -> buildSearchCardsQuery(
                    additionalClause = "code IN (${deckInfo.extraSlots.joinToString { "?" }})",
                    orderByClause = "(set_type_id IS NULL), set_type_id, set_id, set_position",
                    extraSlots = deckInfo.extraSlots,
                    taboo = deckInfo.taboo,
                    filterOptions = filterOptions
                )
            }
        }

        // Create a Pager that wraps the PagingSource from the DAO.
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 40
            ),
            pagingSourceFactory = { deckDao.searchCardsRaw(rawQuery) }
        ).flow
    }

    override fun searchCards(
        filterOptions: CardFilterOptions,
        deckInfo: DeckInfo,
        includeEnglish: Boolean,
        typeIndex: Int,
        showAllSpoilers: Boolean,
        language: String,
        packIds: List<String>
    ): Flow<PagingData<CardDeckListItemProjection>> {
        // Build the FTS query string
        val ftsQuery = if (language == "ru") {
            val stemedString = filterOptions.searchQuery
                .replace("\"(\\[\"]|.*)?\"".toRegex(), " ")
                .split("[^\\p{Alnum}]+".toRegex())
                .filter { it.isNotBlank() }
                .joinToString(separator = " ", transform = { "${PorterStem.stem(it)}*" })
            createQueryString(stemedString, includeEnglish, language)
        } else {
            val stemedString = filterOptions.searchQuery
                .lowercase(Locale.forLanguageTag(language))
                .replace("\"(\\[\"]|.*)?\"".toRegex(), " ")
                .split("[^\\p{Alnum}]+".toRegex())
                .filter { it.isNotBlank() }
                .joinToString(separator = " ", transform = { "$it*" })
            createQueryString(stemedString, includeEnglish, language)
        }
        val newOptions = filterOptions.copy(searchQuery = ftsQuery)

        val rawQuery = if (!deckInfo.isUpgrade) {
            when(typeIndex) {
                0 -> buildSearchCardsQuery(
                    additionalClause = "set_id = 'personality'",
                    orderByClause = "aspect_id, set_position",
                    taboo = deckInfo.taboo,
                    isPacksNeeded = true,
                    packIds = packIds,
                    filterOptions = newOptions
                )
                1 -> buildSearchCardsQuery(
                    additionalClause = "set_id = ? AND set_type_id = 'background' AND type_id != 'role'",
                    orderByClause = "aspect_id, set_position",
                    background = deckInfo.background,
                    taboo = deckInfo.taboo,
                    isPacksNeeded = true,
                    packIds = packIds,
                    filterOptions = newOptions
                )
                2 -> buildSearchCardsQuery(
                    additionalClause = "set_id = ? AND set_type_id = 'specialty' AND type_id != 'role'",
                    orderByClause = "aspect_id, set_position",
                    specialty = deckInfo.specialty,
                    taboo = deckInfo.taboo,
                    isPacksNeeded = true,
                    packIds = packIds,
                    filterOptions = newOptions
                )
                else -> buildSearchCardsQuery(
                    additionalClause = "set_id != ? AND set_id != ? AND type_id != 'role' AND set_id != 'personality' AND real_traits NOT LIKE '%expert%'",
                    orderByClause = "(set_type_id IS NULL), set_type_id, set_id, set_position",
                    background = deckInfo.background,
                    specialty = deckInfo.specialty,
                    taboo = deckInfo.taboo,
                    isPacksNeeded = true,
                    packIds = packIds,
                    filterOptions = newOptions
                )
            }
        } else {
            when(typeIndex) {
                0 -> if (showAllSpoilers) buildSearchCardsQuery(
                    additionalClause = "set_id == 'reward'",
                    orderByClause = "(set_type_id IS NULL), set_type_id, set_id, set_position",
                    taboo = deckInfo.taboo,
                    isPacksNeeded = true,
                    packIds = packIds,
                    filterOptions = newOptions
                ) else buildSearchCardsQuery(
                    additionalClause = "code IN (${deckInfo.rewards.joinToString { "?" }})",
                    orderByClause = "(set_type_id IS NULL), set_type_id, set_id, set_position",
                    rewards = deckInfo.rewards,
                    taboo = deckInfo.taboo,
                    filterOptions = newOptions
                )
                1 -> buildSearchCardsQuery(
                    additionalClause = "set_id == 'malady'",
                    orderByClause = "(set_type_id IS NULL), set_type_id, set_id, set_position",
                    taboo = deckInfo.taboo,
                    isPacksNeeded = true,
                    packIds = packIds,
                    filterOptions = newOptions
                )
                2 -> buildSearchCardsQuery(
                    additionalClause = "spoiler = 'false' OR (spoiler IS NULL AND NOT EXISTS (SELECT 1 FROM card WHERE spoiler = 'false')) AND type_id != 'role'",
                    orderByClause = "(set_type_id IS NULL), set_type_id, set_id, set_position",
                    taboo = deckInfo.taboo,
                    isPacksNeeded = true,
                    packIds = packIds,
                    filterOptions = newOptions
                )
                else -> buildSearchCardsQuery(
                    additionalClause = "code IN (${deckInfo.extraSlots.joinToString { "?" }})",
                    orderByClause = "(set_type_id IS NULL), set_type_id, set_id, set_position",
                    extraSlots = deckInfo.extraSlots,
                    taboo = deckInfo.taboo,
                    filterOptions = newOptions
                )
            }
        }

        // Create a Pager that wraps the PagingSource from the DAO.
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 40
            ),
            pagingSourceFactory = { deckDao.searchCardsRaw(rawQuery) }
        ).flow
    }

    private fun createQueryString(searchQuery: String, includeEnglish: Boolean, language: String): String {
        return if (!includeEnglish || language == "en") "composite:($searchQuery)"
        else "real_composite:($searchQuery)"
    }

    private fun buildSearchCardsQuery(
        additionalClause: String = "",
        orderByClause: String,
        background: String = "",
        specialty: String = "",
        rewards: List<String> = emptyList(),
        extraSlots: List<String> = emptyList(),
        taboo: String? = null,
        isPacksNeeded: Boolean = false,
        packIds: List<String> = emptyList(),
        filterOptions: CardFilterOptions
    ): SimpleSQLiteQuery {
        val isNotEmpty = filterOptions.searchQuery.isNotEmpty()
        val isFilteredPacks = filterOptions.packs.isNotEmpty()
        val packsString = if (isPacksNeeded) {
            if (isFilteredPacks) filterOptions.packs.joinToString { "?" }
            else packIds.joinToString { "?" }
        } else ""
        val filtersClause = CardFilters.buildFiltersClause(filterOptions)
        val sortClause = CardFilters.buildSortClause(filterOptions)
        val defaultSortClause = "(set_type_id IS NULL), set_type_id, set_id, set_position"
        val sql = StringBuilder().apply {
            append("""
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, 
            name, type_name, traits, real_traits, level, set_id, set_type_id, deck_limit, equip,
            approach_connection, approach_reason, approach_conflict, approach_exploration
            FROM (
        """.trimIndent())

            // Case 1: taboo override cards
            append("""
            SELECT card.id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, equip, pack_id,
                real_image_src, name, type_name, traits, real_traits, level, set_id, set_type_id, 
                set_position, deck_limit, approach_connection, approach_reason, approach_conflict, approach_exploration
            FROM card
            ${if (isNotEmpty) "JOIN card_fts ON card.id = card_fts.id" else ""}
            WHERE $additionalClause
              ${if (isNotEmpty) "AND (card_fts MATCH ?)" else ""}
              AND (? IS NOT NULL) AND (taboo_id = ?)
              ${if (packsString.isNotEmpty()) "AND pack_id IN ($packsString)" else ""}
              ${if (filtersClause.isNotEmpty()) "AND ($filtersClause)" else ""}
        """.trimIndent())
            append("\nUNION ALL\n")

            // Case 2: default card when taboo override absent
            append("""
            SELECT card.id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, equip, pack_id,
                real_image_src, name, type_name, traits, real_traits, level, set_id, set_type_id, 
                set_position, deck_limit, approach_connection, approach_reason, approach_conflict, approach_exploration
            FROM card
            ${if (isNotEmpty) "JOIN card_fts ON card.id = card_fts.id" else ""}
            WHERE $additionalClause
              ${if (isNotEmpty) "AND (card_fts MATCH ?)" else ""}
              AND (? IS NOT NULL)
              AND NOT EXISTS (
                  SELECT 1 FROM card t
                  WHERE t.code = card.code
                    AND t.taboo_id = ?
              )
              ${if (packsString.isNotEmpty()) "AND pack_id IN ($packsString)" else ""}
              ${if (filtersClause.isNotEmpty()) "AND ($filtersClause)" else ""}
        """.trimIndent())
            append("\nUNION ALL\n")

            // Case 3: no taboo
            append("""
            SELECT card.id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, equip, pack_id,
                real_image_src, name, type_name, traits, real_traits, level, set_id, set_type_id, 
                set_position, deck_limit, approach_connection, approach_reason, approach_conflict, approach_exploration
            FROM card
            ${if (isNotEmpty) "JOIN card_fts ON card.id = card_fts.id" else ""}
            WHERE $additionalClause
              ${if (isNotEmpty) "AND (card_fts MATCH ?)" else ""}
              AND (? IS NULL AND taboo_id IS NULL)
              ${if (packsString.isNotEmpty()) "AND pack_id IN ($packsString)" else ""}
              ${if (filtersClause.isNotEmpty()) "AND ($filtersClause)" else ""}
        """.trimIndent())

            append("""
            ) 
            ORDER BY ${if (sortClause != defaultSortClause) sortClause else orderByClause}
        """.trimIndent())
        }

        // now collect args in the exact same order as the placeholders
        val args = mutableListOf<Any?>().apply {
            repeat(2) {
                if (background.isNotEmpty()) add(background)
                if (specialty.isNotEmpty()) add(specialty)
                if (rewards.isNotEmpty()) addAll(rewards)
                if (extraSlots.isNotEmpty()) addAll(extraSlots)
                if (isNotEmpty) add(filterOptions.searchQuery)
                repeat(2) { add(taboo) }
                if (packsString.isNotEmpty()) addAll(if (isFilteredPacks) filterOptions.packs else packIds)
            }
            if (background.isNotEmpty()) add(background)
            if (specialty.isNotEmpty()) add(specialty)
            if (rewards.isNotEmpty()) addAll(rewards)
            if (extraSlots.isNotEmpty()) addAll(extraSlots)
            if (isNotEmpty) add(filterOptions.searchQuery)
            add(taboo)
            if (packsString.isNotEmpty()) addAll(if (isFilteredPacks) filterOptions.packs else packIds)
        }

        return SimpleSQLiteQuery(sql.toString(), args.toTypedArray())
    }

    override suspend fun getAllVersionDeckIds(startId: String): List<String> = deckDao.getAllVersionDeckIds(startId)
}