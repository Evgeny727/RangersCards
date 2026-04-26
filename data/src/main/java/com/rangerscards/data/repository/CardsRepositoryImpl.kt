package com.rangerscards.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.sqlite.db.SimpleSQLiteQuery
import com.rangerscards.data.local.dao.CardDao
import com.rangerscards.data.mapper.toDbCards
import com.rangerscards.data.mapper.toDomain
import com.rangerscards.data.objects.CardFilterQueryBuilder
import com.rangerscards.data.objects.PorterStem
import com.rangerscards.data.remote.CardsRemoteDataSource
import com.rangerscards.domain.TimestampNormilizer
import com.rangerscards.domain.model.CardDeckListItem
import com.rangerscards.domain.model.CardFilterOptions
import com.rangerscards.domain.model.CardListItem
import com.rangerscards.domain.model.DeckInfo
import com.rangerscards.domain.model.RoleCard
import com.rangerscards.domain.repository.CardsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale
import javax.inject.Inject

class CardsRepositoryImpl @Inject constructor(
    private val cardsRemoteDataSource: CardsRemoteDataSource,
    private val cardDao: CardDao,
) : CardsRepository {

    override suspend fun downloadAllCards(locale: String) = runCatching {
        val cards = cardsRemoteDataSource.fetchAllCards(locale).dataAssertNoErrors
        cardDao.upsertAll(cards.cards.toDbCards(locale))
        cards.all_updated_at.getOrNull(0)?.updated_at.toString()
    }

    override suspend fun isCardsTableExists(): Boolean = cardDao.isExists()

    override suspend fun isCardsUpdateAvailable(locale: String, savedTimestamp: String) = runCatching {
        val updatedAt = cardsRemoteDataSource.fetchCardsUpdatedAt(locale).dataAssertNoErrors.card_updated_at
        TimestampNormilizer.compareTimestamps(
            savedTimestamp,
            updatedAt.getOrNull(0)?.updated_at.toString()
        )
    }

    override fun getCardByCodeFlow(cardCode: String, taboo: Boolean) =
        cardDao.getCardByCodeFlow(cardCode, taboo).map { it.toDomain() }

    override fun getRoleCardByCodeFlow(code: String, taboo: Boolean): Flow<RoleCard> =
        cardDao.getRoleByCode(code, taboo).map { it.toDomain() }

    override fun getRoleCardsByIdFlow(ids: List<String>): Flow<List<RoleCard>> =
        cardDao.getRolesImages(ids).map { list -> list.map { it.toDomain() } }

    override fun getRewards(taboo: Boolean, packIds: List<String>): Flow<List<CardListItem>> =
        cardDao.getAllRewards(taboo, packIds).map { list -> list.map { it.toDomain() } }

    override fun getDeckCardsByIdFlow(ids: List<String>, tabooId: String?) =
        cardDao.getCardsByCodes(ids, tabooId).map { list -> list.map { it.toDomain() } }

    override suspend fun getChangedDeckCardsById(ids: List<String>, tabooId: String?) =
        cardDao.getChangedCardsByCodes(ids, tabooId).map { it.toDomain() }

    override fun getAllPaginatedRoleCardsFlow(
        specialty: String,
        taboo: Boolean,
        packIds: List<String>
    ): Flow<PagingData<RoleCard>> {
        return Pager(
            config = PagingConfig(
                pageSize = 5,
                enablePlaceholders = false,
                initialLoadSize = 5
            ),
            pagingSourceFactory = { cardDao.getPaginatedRoles(specialty, taboo, packIds) }
        ).flow.map { rolePagingData ->
            rolePagingData.map { it.toDomain() }
        }
    }

    override fun searchPaginatedCardsFlow(
        filterOptions: CardFilterOptions,
        spoiler: Boolean,
        taboo: Boolean,
        packIds: List<String>,
        includeEnglish: Boolean?,
    ): Flow<PagingData<CardListItem>> {
        val newOptions = if (includeEnglish != null) {
            // Build the FTS query string
            val ftsQuery = createQueryString(filterOptions.searchQuery, includeEnglish)
            filterOptions.copy(searchQuery = ftsQuery)
        } else filterOptions
        val rawQuery = buildSearchCardsQuery(spoiler, taboo, packIds, newOptions)
        // Create a Pager that wraps the PagingSource from the DAO.
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 40
            ),
            pagingSourceFactory = { cardDao.searchCardsRaw(rawQuery) }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }

    override fun searchPaginatedDeckCardsFlow(
        filterOptions: CardFilterOptions,
        deckInfo: DeckInfo,
        typeIndex: Int,
        showAllSpoilers: Boolean,
        packIds: List<String>,
        includeEnglish: Boolean?,
    ): Flow<PagingData<CardDeckListItem>> {
        val newOptions = if (includeEnglish != null) {
            // Build the FTS query string
            val ftsQuery = createQueryString(filterOptions.searchQuery, includeEnglish)
            filterOptions.copy(searchQuery = ftsQuery)
        } else filterOptions

        val rawQuery = if (!deckInfo.isUpgrade) {
            when(typeIndex) {
                0 -> buildSearchDeckCardsQuery(
                    additionalClause = "set_id = 'personality'",
                    orderByClause = "aspect_id, set_position",
                    taboo = deckInfo.taboo,
                    isPacksNeeded = true,
                    packIds = packIds,
                    filterOptions = newOptions
                )
                1 -> buildSearchDeckCardsQuery(
                    additionalClause = "set_id = ? AND set_type_id = 'background' AND type_id != 'role'",
                    orderByClause = "aspect_id, set_position",
                    background = deckInfo.background,
                    taboo = deckInfo.taboo,
                    isPacksNeeded = true,
                    packIds = packIds,
                    filterOptions = newOptions
                )
                2 -> buildSearchDeckCardsQuery(
                    additionalClause = "set_id = ? AND set_type_id = 'specialty' AND type_id != 'role'",
                    orderByClause = "aspect_id, set_position",
                    specialty = deckInfo.specialty,
                    taboo = deckInfo.taboo,
                    isPacksNeeded = true,
                    packIds = packIds,
                    filterOptions = newOptions
                )
                else -> buildSearchDeckCardsQuery(
                    additionalClause = "set_id != ? AND set_id != ? AND type_id != 'role' " +
                            "AND set_id != 'personality' AND real_traits NOT LIKE '%expert%' " +
                            "AND set_id != 'reward' AND set_id != 'malady'",
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
                0 -> if (showAllSpoilers) buildSearchDeckCardsQuery(
                    additionalClause = "set_id == 'reward'",
                    orderByClause = "(set_type_id IS NULL), set_type_id, set_id, set_position",
                    taboo = deckInfo.taboo,
                    isPacksNeeded = true,
                    packIds = packIds,
                    filterOptions = newOptions
                ) else buildSearchDeckCardsQuery(
                    additionalClause = "code IN (${deckInfo.rewards.joinToString { "?" }})",
                    orderByClause = "(set_type_id IS NULL), set_type_id, set_id, set_position",
                    rewards = deckInfo.rewards,
                    taboo = deckInfo.taboo,
                    filterOptions = newOptions
                )
                1 -> buildSearchDeckCardsQuery(
                    additionalClause = "set_id == 'malady'",
                    orderByClause = "(set_type_id IS NULL), set_type_id, set_id, set_position",
                    taboo = deckInfo.taboo,
                    isPacksNeeded = true,
                    packIds = packIds,
                    filterOptions = newOptions
                )
                2 -> buildSearchDeckCardsQuery(
                    additionalClause = "(spoiler = 'false' OR (spoiler IS NULL AND NOT EXISTS (SELECT 1 FROM card WHERE spoiler = 'false'))) AND type_id != 'role'",
                    orderByClause = "(set_type_id IS NULL), set_type_id, set_id, set_position",
                    taboo = deckInfo.taboo,
                    isPacksNeeded = true,
                    packIds = packIds,
                    filterOptions = newOptions
                )
                else -> buildSearchDeckCardsQuery(
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
            pagingSourceFactory = { cardDao.searchDeckCardsRaw(rawQuery) }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }

    private fun createQueryString(
        searchQuery: String,
        includeEnglish: Boolean
    ): String {
        val language = Locale.getDefault().language.take(2)
        val stemedString = if (language == "ru") {
            searchQuery.replace("\"(\\[\"]|.*)?\"".toRegex(), " ")
                .split("[^\\p{Alnum}]+".toRegex())
                .filter { it.isNotBlank() }
                .joinToString(separator = " ", transform = { "${PorterStem.stem(it)}*" })
        } else {
            searchQuery.lowercase(Locale.forLanguageTag(language))
                .replace("\"(\\[\"]|.*)?\"".toRegex(), " ")
                .split("[^\\p{Alnum}]+".toRegex())
                .filter { it.isNotBlank() }
                .joinToString(separator = " ", transform = { "$it*" })
        }
        return if (!includeEnglish || language == "en") "composite:($stemedString)"
        else "real_composite:($stemedString)"
    }

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
        val filtersClause = CardFilterQueryBuilder.buildFiltersClause(filterOptions)
        val sortClause = CardFilterQueryBuilder.buildSortClause(filterOptions)
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

    private fun buildSearchDeckCardsQuery(
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
        val filtersClause = CardFilterQueryBuilder.buildFiltersClause(filterOptions)
        val sortClause = CardFilterQueryBuilder.buildSortClause(filterOptions)
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
}