package com.rangerscards.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.room.withTransaction
import com.rangerscards.data.local.RangersDatabase
import com.rangerscards.data.local.dao.CampaignDao
import com.rangerscards.data.local.dao.DeckDao
import com.rangerscards.data.mapper.toDbCampaign
import com.rangerscards.data.mapper.toDbDeck
import com.rangerscards.data.mapper.toDbDecks
import com.rangerscards.data.mapper.toDomain
import com.rangerscards.data.mapper.toJsonDeckMeta
import com.rangerscards.data.mapper.toJsonDeckSlots
import com.rangerscards.data.remote.DecksRemoteDataSource
import com.rangerscards.domain.TimestampNormilizer.getCurrentDateTime
import com.rangerscards.domain.model.Deck
import com.rangerscards.domain.model.DeckCampaignInfo
import com.rangerscards.domain.model.DeckListItem
import com.rangerscards.domain.model.DeckMeta
import com.rangerscards.domain.repository.DecksRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import com.rangerscards.data.local.deck.Deck as DbDeck


class DecksRepositoryImpl @Inject constructor(
    private val decksRemoteDataSource: DecksRemoteDataSource,
    private val db: RangersDatabase,
    private val deckDao: DeckDao,
    private val campaignDao: CampaignDao
) : DecksRepository {

    override suspend fun deleteAllLocalDecks() = deckDao.deleteAllLocalDecks()

    override suspend fun syncDecks(userId: String) = runCatching {
        val networkDecks = decksRemoteDataSource.fetchDecks(userId).dataAssertNoErrors
        deckDao.syncDecks(networkDecks.decks.toDbDecks())
    }

    override suspend fun syncDeckById(id: Int) = runCatching {
        val networkDeck = decksRemoteDataSource.fetchDeckById(id).dataAssertNoErrors
        deckDao.upsertDeck(networkDeck.deck!!.deck.toDbDeck())
    }

    override fun getAllPaginatedDecksFlow(
        userId: String,
        uploaded: Boolean?
    ): Flow<PagingData<DeckListItem>> {
        // Create a Pager that wraps the PagingSource from the DAO.
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false,
                initialLoadSize = 20
            ),
            pagingSourceFactory = { deckDao.getAllDecks(userId, uploaded) }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }

    override fun searchPaginatedDecksFlow(
        query: String,
        userId: String,
        uploaded: Boolean?
    ): Flow<PagingData<DeckListItem>> {
        val newQuery = query
            .lowercase()
            .replace("\"(\\[\"]|.*)?\"".toRegex(), " ")
            .split("[^\\p{Alnum}]+".toRegex())
            .filter { it.isNotBlank() }
            .joinToString(separator = " ", transform = { "%$it%" })
        // Create a Pager that wraps the PagingSource from the DAO.
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false,
                initialLoadSize = 20
            ),
            pagingSourceFactory = { deckDao.searchDecks(newQuery, userId, uploaded) }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun createDeck(
        uploaded: Boolean,
        name: String,
        slots: ImmutableMap<String, Int>,
        extraSlots: ImmutableMap<String, Int>,
        meta: DeckMeta,
        tabooSetId: String?,
        awa: Int?,
        spi: Int?,
        fit: Int?,
        foc: Int?,
    ) = runCatching {
        if (uploaded) {
            val newDeck = decksRemoteDataSource.createDeck(
                name = name,
                foc = foc ?: 3,
                fit = fit ?: 3,
                awa = awa ?: 3,
                spi = spi ?: 3,
                meta = meta.toJsonDeckMeta(),
                slots = slots.toJsonDeckSlots(),
                extraSlots = extraSlots.toJsonDeckSlots(),
                tabooSetId = tabooSetId
            ).dataAssertNoErrors.deck!!.deck.toDbDeck()
            deckDao.insertDeck(newDeck)
            newDeck.id
        } else {
            val uuid = Uuid.random().toString()
            val localDeck = createLocalDeck(
                id = uuid,
                name = name,
                slots = slots.toJsonDeckSlots(),
                extraSlots = extraSlots.toJsonDeckSlots(),
                meta = meta.toJsonDeckMeta(),
                tabooSetId = tabooSetId,
                awa = awa,
                spi = spi,
                fit = fit,
                foc = foc
            )
            deckDao.insertDeck(localDeck)
            uuid
        }
    }

    private fun createLocalDeck(
        id: String,
        name: String,
        slots: JsonElement,
        extraSlots: JsonElement,
        meta: JsonElement,
        tabooSetId: String?,
        awa: Int?,
        spi: Int?,
        fit: Int?,
        foc: Int?,
    ): DbDeck {
        val currentTime = getCurrentDateTime()
        return DbDeck(
            id = id,
            uploaded = false,
            userId = "",
            tabooSetId = tabooSetId,
            userHandle = null,
            slots = slots,
            sideSlots = extraSlots,
            extraSlots = JsonObject(emptyMap()),
            version = 1,
            name = name,
            description = null,
            awa = awa ?: 3,
            spi = spi ?: 3,
            fit = fit ?: 3,
            foc = foc ?: 3,
            createdAt = currentTime,
            updatedAt = currentTime,
            meta = meta,
            campaignId = null,
            campaignName = null,
            campaignRewards = null,
            previousId = null,
            previousSlots = null,
            previousSideSlots = null,
            nextId = null,
        )
    }

    override fun getDeckByIdFlow(id: String): Flow<Deck> =
        deckDao.getDeckByIdFlow(id).mapNotNull { it?.toDomain() }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun upgradeDeck(id: String, uploaded: Boolean) = runCatching {
        if (uploaded) {
            val upgradedDeck = decksRemoteDataSource.upgradeDeck(id.toInt())
                .dataAssertNoErrors.deck!!.deck.toDbDeck()
            val nextDeck = decksRemoteDataSource.fetchDeckById(upgradedDeck.nextId!!.toInt())
                .dataAssertNoErrors.deck!!.deck.toDbDeck()
            db.withTransaction {
                deckDao.updateDeck(upgradedDeck)
                deckDao.insertDeck(nextDeck)
            }
            nextDeck.id
        } else {
            val newUuid = Uuid.random().toString()
            val localDeck = deckDao.getDeckById(id)!!
            val currentTime = getCurrentDateTime()

            db.withTransaction {
                var newRewards = localDeck.campaignRewards

                localDeck.campaignId?.let {
                    val campaign = campaignDao.getCampaignById(localDeck.campaignId)
                    campaign?.let {
                        newRewards = campaign.rewards
                        val newDeck = buildJsonArray {
                            add(localDeck.name)
                            add(localDeck.meta)
                            add(campaign.latestDecks.jsonObject[localDeck.id]?.jsonArray?.get(2)?.jsonObject
                                ?: JsonObject(emptyMap())
                            )
                        }
                        val newDeckValues = buildJsonObject {
                            campaign.latestDecks.jsonObject.forEach { (key, value) ->
                                if (key == localDeck.id) {
                                    put(newUuid, newDeck)  // Replace the target key
                                } else {
                                    put(key, value)  // Keep other keys unchanged
                                }
                            }
                        }
                        campaignDao.updateCampaign(campaign.copy(
                            latestDecks = newDeckValues,
                            updatedAt = currentTime
                        ))
                    }
                }

                deckDao.updateDeck(localDeck.copy(
                    nextId = newUuid,
                    updatedAt = currentTime
                ))

                deckDao.insertDeck(localDeck.copy(
                    id = newUuid,
                    previousId = localDeck.id,
                    version = localDeck.version + 1,
                    previousSlots = localDeck.slots,
                    previousSideSlots = localDeck.sideSlots,
                    createdAt = currentTime,
                    updatedAt = currentTime,
                    campaignRewards = newRewards
                ))
            }

            newUuid
        }
    }

    override suspend fun saveDeck(deck: Deck) = runCatching {
        if (deck.uploaded) {
            val updatedDeck = decksRemoteDataSource.saveDeck(
                deckId = deck.id.toInt(),
                name = deck.name,
                foc = deck.oftenUpdatableDeckValues.foc,
                fit = deck.oftenUpdatableDeckValues.fit,
                awa = deck.oftenUpdatableDeckValues.awa,
                spi = deck.oftenUpdatableDeckValues.spi,
                meta = deck.deckMeta.toJsonDeckMeta(),
                slots = deck.oftenUpdatableDeckValues.slots.toJsonDeckSlots(),
                sideSlots = deck.oftenUpdatableDeckValues.sideSlots.toJsonDeckSlots(),
                extraSlots = deck.oftenUpdatableDeckValues.extraSlots.toJsonDeckSlots()
            ).dataAssertNoErrors.update_rangers_deck_by_pk!!.deck.toDbDeck()
            deckDao.updateDeck(updatedDeck)
        } else deckDao.updateDeck(deck.toDbDeck())
    }

    override suspend fun saveDeckTabooSet(id: String, tabooId: String?, uploaded: Boolean) =
        runCatching {
            if (uploaded) {
                val updatedDeck = decksRemoteDataSource.saveDeckTabooSet(
                    id.toInt(),
                    tabooId
                ).dataAssertNoErrors.update_rangers_deck_by_pk!!.deck.toDbDeck()
                deckDao.updateDeck(updatedDeck)
            } else {
                val localDeck = deckDao.getDeckById(id)!!
                deckDao.updateDeck(localDeck.copy(
                    tabooSetId = tabooId,
                    updatedAt = getCurrentDateTime()
                ))
            }
        }

    override suspend fun setDeckCampaign(
        id: String,
        campaignInfo: DeckCampaignInfo,
        uploaded: Boolean
    ) = runCatching {
        if (uploaded) {
            decksRemoteDataSource.setDeckCampaign(
                id.toInt(),
                campaignInfo.campaignId.toInt()
            ).dataAssertNoErrors.campaign.map { campaign -> campaign.campaign.toDbCampaign() }
            val updatedDeck = decksRemoteDataSource.fetchDeckById(id.toInt())
                .dataAssertNoErrors.deck!!.deck.toDbDeck()
            deckDao.updateDeck(updatedDeck)
        } else {
            val localDeck = deckDao.getDeckById(id)!!
            val campaignEntry = campaignDao.getCampaignById(campaignInfo.campaignId)!!
            val currentTime = getCurrentDateTime()

            db.withTransaction {
                deckDao.updateDeck(localDeck.copy(
                    campaignId = campaignInfo.campaignId,
                    campaignName = campaignInfo.campaignName,
                    campaignRewards = buildJsonArray { campaignInfo.campaignRewards.forEach { add(it) } },
                    updatedAt = currentTime
                ))
                val newDeckJson = buildJsonArray {
                    add(localDeck.name)
                    add(localDeck.meta)
                    add(buildJsonObject {
                        put(localDeck.userId, localDeck.userHandle)
                    })
                }
                campaignDao.updateCampaign(campaignEntry.copy(
                    latestDecks = JsonObject(
                        campaignEntry.latestDecks.jsonObject + (id to newDeckJson)
                    ),
                    updatedAt = currentTime
                ))
            }
        }
    }

    override suspend fun removeDeckCampaign(
        id: String,
        campaignInfo: DeckCampaignInfo,
        uploaded: Boolean
    ) = runCatching {
        if (uploaded) {
            decksRemoteDataSource.removeDeckCampaign(
                id.toInt(),
                campaignInfo.campaignId.toInt()
            ).dataAssertNoErrors
            val updatedDeck = decksRemoteDataSource.fetchDeckById(id.toInt())
                .dataAssertNoErrors.deck!!.deck.toDbDeck()
            deckDao.updateDeck(updatedDeck)
        } else {
            val localDeck = deckDao.getDeckById(id)!!
            val campaign = campaignDao.getCampaignById(campaignInfo.campaignId)
            val currentTime = getCurrentDateTime()

            db.withTransaction {
                deckDao.updateDeck(localDeck.copy(
                    updatedAt = currentTime,
                    campaignId = null,
                    campaignName = null,
                    campaignRewards = null
                ))
                campaign?.let {
                    campaignDao.updateCampaign(campaign.copy(
                        latestDecks = JsonObject(
                            campaign.latestDecks.jsonObject.filterKeys { it != id }
                        ),
                        updatedAt = currentTime
                    ))
                }
                Unit
            }
        }
    }

    override suspend fun deleteDeckById(id: String, uploaded: Boolean) = runCatching {
        val localDeck = deckDao.getDeckById(id)!!
        if (uploaded) {
            decksRemoteDataSource.deleteDeckById(id.toInt()).dataAssertNoErrors
            db.withTransaction {
                deckDao.deleteDeckById(id)
                val previousId = localDeck.previousId
                previousId?.let {
                    val previousDeck = deckDao.getDeckById(previousId)!!
                    deckDao.updateDeck(previousDeck.copy(nextId = null,
                        updatedAt = getCurrentDateTime()))
                    previousId
                }
            }
        } else {
            db.withTransaction {
                deckDao.deleteDeckById(id)
                val previousId = localDeck.previousId
                val currentTime = getCurrentDateTime()
                if (previousId != null) {
                    val previousDeck = deckDao.getDeckById(previousId)!!
                    deckDao.updateDeck(previousDeck.copy(
                        nextId = null,
                        campaignId = localDeck.campaignId,
                        campaignName = localDeck.campaignName,
                        updatedAt = currentTime
                    ))
                    localDeck.campaignId?.let {
                        val campaign = campaignDao.getCampaignById(localDeck.campaignId)
                        campaign?.let {
                            val oldDeckValue = campaign.latestDecks.jsonObject[localDeck.id]!!.jsonArray
                            val newDeckValues = buildJsonObject {
                                campaign.latestDecks.jsonObject.forEach { (key, value) ->
                                    if (key == localDeck.id) {
                                        put(previousId, oldDeckValue)  // Replace the target key
                                    } else {
                                        put(key, value)  // Keep other keys unchanged
                                    }
                                }
                            }
                            campaignDao.updateCampaign(campaign.copy(
                                latestDecks = newDeckValues,
                                updatedAt = currentTime
                            ))
                        }
                    }
                    previousId
                } else {
                    localDeck.campaignId?.let {
                        val campaign = campaignDao.getCampaignById(localDeck.campaignId)
                        campaign?.let {
                            val newDeckValues = buildJsonObject {
                                campaign.latestDecks.jsonObject.forEach { (key, value) ->
                                    if (key != localDeck.id) {
                                        put(key, value)  // Keep other keys unchanged
                                    }
                                }
                            }
                            campaignDao.updateCampaign(campaign.copy(
                                latestDecks = newDeckValues,
                                updatedAt = currentTime
                            ))
                        }
                    }
                    null
                }
            }
        }
    }

    override suspend fun deleteAllDeckVersionsById(id: String, uploaded: Boolean) = runCatching {
        val deck = deckDao.getDeckById(id)!!
        val deckIds = deckDao.getAllVersionDeckIds(id)

        if (deck.uploaded) {
            deckIds.forEach { deckId ->
                decksRemoteDataSource.deleteDeckById(deckId.toInt()).dataAssertNoErrors
            }
        }

        db.withTransaction {
            deck.campaignId?.let {
                val campaign = campaignDao.getCampaignById(deck.campaignId)
                campaign?.let {
                    val newDeckValues = buildJsonObject {
                        campaign.latestDecks.jsonObject.forEach { (key, value) ->
                            if (key != deck.id) {
                                put(key, value)  // Keep other keys unchanged
                            }
                        }
                    }
                    campaignDao.updateCampaign(campaign.copy(
                        latestDecks = newDeckValues,
                        updatedAt = getCurrentDateTime()
                    ))
                }
            }

            deckDao.deleteDecksById(deckIds)
        }
    }

    override suspend fun getAllDeckVersionIds(startId: String): ImmutableList<String> =
        deckDao.getAllVersionDeckIds(startId).toImmutableList()
}