package com.rangerscards.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.sqlite.db.SimpleSQLiteQuery
import com.rangerscards.objects.CardFilterOptions
import com.rangerscards.objects.CardFilters
import com.rangerscards.data.local.card.Card
import com.rangerscards.data.local.card.CardListItemProjection
import com.rangerscards.data.local.card.FullCardProjection
import com.rangerscards.data.local.dao.CardDao
import com.rangerscards.data.objects.PorterStem
import com.rangerscards.domain.repository.CardsRepository
import kotlinx.coroutines.flow.Flow
import java.util.Locale

class OfflineCardsRepository(private val cardDao: CardDao) : CardsRepository {

    override suspend fun insertAllCards(cards: List<Card>) = cardDao.insertAll(cards)

    override suspend fun upsertAllCards(cards: List<Card>) = cardDao.upsertAll(cards)

    override suspend fun isExists(): Boolean = cardDao.isExists()

    override fun getAllCards(
        spoiler: Boolean,
        taboo: Boolean,
        packIds: List<String>,
        filterOptions: CardFilterOptions
    ): Flow<PagingData<CardListItemProjection>> {
        val rawQuery = buildSearchCardsQuery(spoiler, taboo, packIds, filterOptions)
        // Create a Pager that wraps the PagingSource from the DAO.
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 40
            ),
            pagingSourceFactory = { cardDao.searchCardsRaw(rawQuery) }
        ).flow
    }

    override fun searchCards(
        filterOptions: CardFilterOptions,
        includeEnglish: Boolean,
        spoiler: Boolean,
        language: String,
        taboo: Boolean,
        packIds: List<String>
    ): Flow<PagingData<CardListItemProjection>> {
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
        val rawQuery = buildSearchCardsQuery(spoiler, taboo, packIds, filterOptions.copy(searchQuery = ftsQuery))
        // Create a Pager that wraps the PagingSource from the DAO.
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 40
            ),
            pagingSourceFactory = { cardDao.searchCardsRaw(rawQuery) }
        ).flow
    }

    private fun createQueryString(searchQuery: String, includeEnglish: Boolean, language: String): String {
        return if (!includeEnglish || language == "en") "composite:($searchQuery)"
        else "real_composite:($searchQuery)"
    }

    override fun getCardByCode(cardCode: String, taboo: Boolean): Flow<FullCardProjection?> =
        cardDao.getCardById(cardCode, taboo)

    private fun buildSearchCardsQuery(
        spoiler: Boolean,
        taboo: Boolean,
        packIds: List<String>,
        filterOptions: CardFilterOptions
    ): SimpleSQLiteQuery {
        val isNotEmpty = filterOptions.searchQuery.isNotEmpty()
        val isFilteredPacks = filterOptions.packs.isNotEmpty()
        val packsString = if (isFilteredPacks) filterOptions.packs.joinToString { "?" }
            else packIds.joinToString { "?" }
        val filtersClause = CardFilters.buildFiltersClause(filterOptions)
        val sortClause = CardFilters.buildSortClause(filterOptions)
        val sql = StringBuilder().apply {
            append("""
            SELECT id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, real_image_src, name, equip,
                   type_name, traits, level, approach_connection, approach_reason, approach_conflict, approach_exploration
            FROM (
        """.trimIndent())

            // Case 1: taboo override cards
            append("""
            SELECT card.id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, equip,
                real_image_src, name, type_name, traits, level, approach_connection, approach_reason, 
                approach_conflict, approach_exploration, set_type_id, set_id, set_position, pack_id
            FROM card
            ${if (isNotEmpty) "JOIN card_fts ON card.id = card_fts.id" else ""}
            WHERE (spoiler = ? OR (spoiler IS NULL AND NOT EXISTS (SELECT 1 FROM card WHERE spoiler = ?)))
              ${if (isNotEmpty) "AND (card_fts MATCH ?)" else ""}
              AND pack_id IN ($packsString)
              AND (? IS 1 AND taboo_id IS NOT NULL)
              ${if (filtersClause.isNotEmpty()) "AND ($filtersClause)" else ""}
        """.trimIndent())
            append("\nUNION ALL\n")

            // Case 2: default card when taboo override absent
            append("""
            SELECT card.id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, equip,
                real_image_src, name, type_name, traits, level, approach_connection, approach_reason, 
                approach_conflict, approach_exploration, set_type_id, set_id, set_position, pack_id
            FROM card
            ${if (isNotEmpty) "JOIN card_fts ON card.id = card_fts.id" else ""}
            WHERE (spoiler = ? OR (spoiler IS NULL AND NOT EXISTS (SELECT 1 FROM card WHERE spoiler = ?)))
              ${if (isNotEmpty) "AND (card_fts MATCH ?)" else ""}
              AND pack_id IN ($packsString)
              AND (? IS 1 AND taboo_id IS NULL)
              AND NOT EXISTS (
                  SELECT 1 FROM card c2
                  WHERE c2.code = card.code
                    AND c2.taboo_id IS NOT NULL
              )
              ${if (filtersClause.isNotEmpty()) "AND ($filtersClause)" else ""}
        """.trimIndent())
            append("\nUNION ALL\n")

            // Case 3: no taboo
            append("""
            SELECT card.id, code, taboo_id, set_name, aspect_id, aspect_short_name, cost, equip,
                real_image_src, name, type_name, traits, level, approach_connection, approach_reason, 
                approach_conflict, approach_exploration, set_type_id, set_id, set_position, pack_id
            FROM card
            ${if (isNotEmpty) "JOIN card_fts ON card.id = card_fts.id" else ""}
            WHERE (spoiler = ? OR (spoiler IS NULL AND NOT EXISTS (SELECT 1 FROM card WHERE spoiler = ?)))
              ${if (isNotEmpty) "AND (card_fts MATCH ?)" else ""}
              AND pack_id IN ($packsString)
              AND (? IS 0 AND taboo_id IS NULL)
              ${if (filtersClause.isNotEmpty()) "AND ($filtersClause)" else ""}
        """.trimIndent())

            append("""
            ) 
            ORDER BY $sortClause
        """.trimIndent())
        }

        // now collect args in the exact same order as the placeholders
        val args = mutableListOf<Any>()

        fun appendOneBlock() {
            // 1-2) spoiler = ?
            repeat(2) { args.add(spoiler) }
            // 3) MATCH ?
            if (isNotEmpty) args.add(filterOptions.searchQuery)
            // 4) pack_id IN (?,?,…)
            args.addAll(if (isFilteredPacks) filterOptions.packs else packIds)
            // 5) (? = 1 OR ? = 0)
            args.add(if (taboo) 1 else 0)
        }
        repeat(3) { appendOneBlock() }

        return SimpleSQLiteQuery(sql.toString(), args.toTypedArray())
    }
}