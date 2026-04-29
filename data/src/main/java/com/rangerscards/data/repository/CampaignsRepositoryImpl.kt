package com.rangerscards.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.room.withTransaction
import com.rangerscards.data.local.RangersDatabase
import com.rangerscards.data.local.campaign.ChallengeDeck
import com.rangerscards.data.local.dao.CampaignDao
import com.rangerscards.data.local.dao.DeckDao
import com.rangerscards.data.mapper.toDbCampaign
import com.rangerscards.data.mapper.toDbCampaigns
import com.rangerscards.data.mapper.toDomain
import com.rangerscards.data.mapper.toJsonCalendar
import com.rangerscards.data.mapper.toJsonEvents
import com.rangerscards.data.mapper.toJsonHistory
import com.rangerscards.data.mapper.toJsonMissions
import com.rangerscards.data.mapper.toJsonNotes
import com.rangerscards.data.mapper.toJsonRemoved
import com.rangerscards.data.remote.CampaignsRemoteDataSource
import com.rangerscards.domain.TimestampNormilizer.getCurrentDateTime
import com.rangerscards.domain.model.Campaign
import com.rangerscards.domain.model.CampaignListItem
import com.rangerscards.domain.repository.CampaignsRepository
import com.rangerscards.domain.repository.RemoteUpdateAction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import com.rangerscards.data.local.campaign.Campaign as DbCampaign

class CampaignsRepositoryImpl @Inject constructor(
    private val campaignsRemoteDataSource: CampaignsRemoteDataSource,
    private val  db: RangersDatabase,
    private val campaignDao: CampaignDao,
    private val deckDao: DeckDao
) : CampaignsRepository {

    override suspend fun deleteAllLocalCampaigns() = campaignDao.deleteAllLocalCampaigns()

    override suspend fun syncCampaigns(userId: String) = runCatching {
        val networkCampaigns = campaignsRemoteDataSource
            .fetchAllCampaigns(userId).dataAssertNoErrors
        campaignDao.syncCampaigns(networkCampaigns.campaigns.toDbCampaigns())
    }

    override fun getAllPaginatedCampaignsFlow(userId: String): Flow<PagingData<CampaignListItem>> {
        // Create a Pager that wraps the PagingSource from the DAO.
        return Pager(
            config = PagingConfig(
                pageSize = 5,
                enablePlaceholders = false,
                initialLoadSize = 10
            ),
            pagingSourceFactory = { campaignDao.getAllCampaigns(userId) }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }

    override fun searchPaginatedCampaignsFlow(query: String, userId: String): Flow<PagingData<CampaignListItem>> {
        val newQuery = query
            .lowercase()
            .replace("\"(\\[\"]|.*)?\"".toRegex(), " ")
            .split("[^\\p{Alnum}]+".toRegex())
            .filter { it.isNotBlank() }
            .joinToString(separator = " ", transform = { "%$it%" })
        // Create a Pager that wraps the PagingSource from the DAO.
        return Pager(
            config = PagingConfig(
                pageSize = 5,
                enablePlaceholders = false,
                initialLoadSize = 10
            ),
            pagingSourceFactory = { campaignDao.searchCampaigns(newQuery, userId) }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }

    override fun getAllPaginatedCampaignsForTransferFlow(cycleId: String, userId: String) =
        Pager(
            config = PagingConfig(
                pageSize = 5,
                enablePlaceholders = false,
                initialLoadSize = 10
            ),
            pagingSourceFactory = { campaignDao.getAllCampaignsForTransfer(cycleId, userId) }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }

    override fun getCampaignFlowById(id: String) =
        campaignDao.getCampaignFlowById(id).mapNotNull { it?.toDomain() }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun createCampaign(
        uploaded: Boolean,
        name: String,
        cycleId: String,
        currentLocation: String,
        expansions: List<String>,
        transferCampaignId: String?
    ) = runCatching {
        if (transferCampaignId == null) {
            if (uploaded) {
                val newCampaign = campaignsRemoteDataSource.createCampaign(
                    name = name,
                    cycleId = cycleId,
                    currentLocation = currentLocation,
                    expansions = buildJsonArray {
                        expansions.forEach { add(it) }
                    },
                    calendar = JsonArray(emptyList())
                ).dataAssertNoErrors.campaign!!.campaign.toDbCampaign()
                campaignDao.insertCampaign(newCampaign)
                newCampaign.id
            } else {
                val uuid = Uuid.random().toString()
                campaignDao.insertCampaign(
                    createLocalCampaign(
                        id = uuid,
                        name = name,
                        cycleId = cycleId,
                        currentLocation = currentLocation,
                        expansions = expansions
                    )
                )
                uuid
            }
        } else {
            val isUploaded = transferCampaignId.toIntOrNull() != null
            if (isUploaded) {
                val newCampaigns = campaignsRemoteDataSource.transferCampaign(
                    campaignId = transferCampaignId.toInt(),
                    cycleId = cycleId,
                    currentLocation = currentLocation
                ).dataAssertNoErrors.campaign.map { it.campaign.toDbCampaign() }
                campaignDao.upsertAllCampaigns(newCampaigns)
                newCampaigns.first().id
            } else {
                val uuid = Uuid.random().toString()
                val previousCampaign = campaignDao.getCampaignById(transferCampaignId)!!
                val currentDateTime = getCurrentDateTime()

                db.withTransaction {
                    campaignDao.upsertAllCampaigns(
                        listOf(
                            previousCampaign.copy(
                                latestDecks = JsonObject(emptyMap()),
                                updatedAt = currentDateTime,
                                nextCampaignId = uuid
                            ),
                            previousCampaign.copy(
                                id = uuid,
                                day = 1,
                                extendedCalendar = null,
                                cycleId = cycleId,
                                currentLocation = currentLocation,
                                currentPathTerrain = null,
                                history = JsonArray(emptyList()),
                                calendar = JsonArray(emptyList()),
                                expansions = JsonArray(emptyList()),
                                createdAt = currentDateTime,
                                updatedAt = currentDateTime,
                                previousCampaignId = previousCampaign.id
                            )
                        )
                    )
                    val decks = deckDao.getDecksById(
                        previousCampaign.latestDecks.jsonObject.keys.toList()
                    )
                    deckDao.upsertAllDecks(decks.map {
                        it.copy(
                            updatedAt = currentDateTime,
                            campaignId = uuid,
                        )
                    })
                }

                uuid
            }
        }
    }

    private fun createLocalCampaign(
        id: String,
        name: String,
        cycleId: String,
        currentLocation: String,
        expansions: List<String>
    ): DbCampaign {
        val currentDateTime = getCurrentDateTime()
        return DbCampaign(
            id = id,
            uploaded = false,
            userId = "",
            name = name,
            notes = JsonArray(emptyList()),
            day = 1,
            extendedCalendar = null,
            cycleId = cycleId,
            currentLocation = currentLocation,
            currentPathTerrain = null,
            missions = JsonArray(emptyList()),
            events = JsonArray(emptyList()),
            rewards = JsonArray(emptyList()),
            removed = JsonArray(emptyList()),
            history = JsonArray(emptyList()),
            calendar = JsonArray(emptyList()),
            createdAt = currentDateTime,
            updatedAt = currentDateTime,
            expansions = buildJsonArray { expansions.forEach { add(it) } },
            latestDecks = JsonObject(emptyMap()),
            access = JsonObject(emptyMap()),
            nextCampaignId = null,
            previousCampaignId = null
        )
    }

    override suspend fun uploadCampaign(campaign: Campaign) = runCatching {
        val createdCampaign = campaignsRemoteDataSource.createCampaign(
            name = campaign.name,
            cycleId = campaign.cycleId,
            currentLocation = campaign.currentLocation,
            expansions = buildJsonArray { campaign.expansions.forEach { add(it) } },
            calendar = campaign.calendar.toJsonCalendar()
        ).dataAssertNoErrors.campaign!!.campaign

        val uploadedCampaign = campaignsRemoteDataSource.updateUploadedCampaign(
            campaignId = createdCampaign.id,
            currentPathTerrain = campaign.currentPathTerrain,
            day = campaign.currentDay,
            extendedCalendar = campaign.extendedCalendar,
            missions = campaign.missions.toJsonMissions(),
            events = campaign.events.toJsonEvents(),
            rewards = buildJsonArray { campaign.rewards.forEach { add(it) } },
            removed = campaign.removed.toJsonRemoved(),
            history = campaign.history.toJsonHistory()
        ).dataAssertNoErrors.campaign!!.campaign.toDbCampaign()

        db.withTransaction {
            campaignDao.insertCampaign(uploadedCampaign)
            campaignDao.deleteCampaignById(campaign.id)
        }

        uploadedCampaign.id
    }

    override suspend fun updateCampaign(
        campaign: Campaign,
        remoteUpdateAction: RemoteUpdateAction
    ) = runCatching {
        if (campaign.uploaded) {
            val campaign = when(remoteUpdateAction) {
                RemoteUpdateAction.SET_EXPANSIONS ->
                    campaignsRemoteDataSource.updateCampaignExpansions(
                        campaignId = campaign.id.toInt(),
                        expansions = buildJsonArray { campaign.expansions.forEach { add(it) } }
                    ).dataAssertNoErrors.campaign!!.campaign
                RemoteUpdateAction.SET_TITLE ->
                    campaignsRemoteDataSource.setCampaignTitle(
                    campaignId = campaign.id.toInt(),
                    title = campaign.name
                ).dataAssertNoErrors.campaign!!.campaign
                RemoteUpdateAction.SET_NOTES ->
                    campaignsRemoteDataSource.setCampaignNotes(
                        campaignId = campaign.id.toInt(),
                        notes = campaign.notes.toJsonNotes()
                    ).dataAssertNoErrors.campaign!!.campaign
                RemoteUpdateAction.SET_REWARDS ->
                    campaignsRemoteDataSource.updateCampaignRewards(
                        campaignId = campaign.id.toInt(),
                        rewards = buildJsonArray { campaign.rewards.forEach { add(it) } }
                    ).dataAssertNoErrors.campaign!!.campaign
                RemoteUpdateAction.SET_EVENTS ->
                    campaignsRemoteDataSource.updateCampaignEvents(
                        campaignId = campaign.id.toInt(),
                        events = campaign.events.toJsonEvents()
                    ).dataAssertNoErrors.campaign!!.campaign
                RemoteUpdateAction.SET_REMOVED ->
                    campaignsRemoteDataSource.updateCampaignRemoved(
                        campaignId = campaign.id.toInt(),
                        removed = campaign.removed.toJsonRemoved()
                    ).dataAssertNoErrors.campaign!!.campaign
                RemoteUpdateAction.EXTEND ->
                    campaignsRemoteDataSource.extendCampaign(
                        campaignId = campaign.id.toInt()
                    ).dataAssertNoErrors.campaign!!.campaign
                RemoteUpdateAction.SET_MISSION ->
                    campaignsRemoteDataSource.setCampaignMissions(
                        campaignId = campaign.id.toInt(),
                        missions = campaign.missions.toJsonMissions()
                    ).dataAssertNoErrors.campaign!!.campaign
                RemoteUpdateAction.SET_CALENDAR ->
                    campaignsRemoteDataSource.setCampaignCalendar(
                        campaignId = campaign.id.toInt(),
                        calendar = campaign.calendar.toJsonCalendar()
                    ).dataAssertNoErrors.campaign!!.campaign
                RemoteUpdateAction.SET_DAY ->
                    campaignsRemoteDataSource.setCampaignDay(
                        campaignId = campaign.id.toInt(),
                        day = campaign.currentDay
                    ).dataAssertNoErrors.campaign!!.campaign
                RemoteUpdateAction.SET_TRAVEL ->
                    campaignsRemoteDataSource.campaignTravel(
                        campaignId = campaign.id.toInt(),
                        day = campaign.currentDay,
                        location = campaign.currentLocation,
                        pathTerrain = campaign.currentPathTerrain,
                        history = campaign.history.toJsonHistory()
                    ).dataAssertNoErrors.campaign!!.campaign
            }.toDbCampaign()
            campaignDao.updateCampaign(campaign)
        } else {
            campaignDao.updateCampaign(campaign.toDbCampaign())
        }
    }

    override suspend fun addFriendToCampaign(campaignId: String, friendUserId: String) = runCatching {
        val newAccess = campaignsRemoteDataSource
            .addFriendToCampaign(campaignId.toInt(), friendUserId)
            .dataAssertNoErrors.access!!.campaign.access.mapNotNull { it.user?.userInfo?.toDomain() }
        val localCampaign = campaignDao.getCampaignById(campaignId)!!
        campaignDao.updateCampaign(localCampaign.copy(
            access = buildJsonObject { newAccess.forEach { put(it.id, it.handle) } }
        ))
    }

    override suspend fun removeFriendFromCampaign(campaignId: String, friendUserId: String) = runCatching {
        val newAccess = campaignsRemoteDataSource
            .removeFriendFromCampaign(campaignId.toInt(), friendUserId)
            .dataAssertNoErrors.access!!.campaign.access.mapNotNull { it.user?.userInfo?.toDomain() }
        val localCampaign = campaignDao.getCampaignById(campaignId)!!
        campaignDao.updateCampaign(localCampaign.copy(
            access = buildJsonObject { newAccess.forEach { put(it.id, it.handle) } }
        ))
    }

    override suspend fun leaveCampaign(campaignId: String, userId: String) = runCatching {
        campaignsRemoteDataSource.leaveCampaign(campaignId.toInt(), userId)
        campaignDao.deleteCampaignById(campaignId)
    }

    override suspend fun deleteCampaignById(id: String, uploaded: Boolean) = runCatching {
        if (uploaded) {
            val updatedData = campaignsRemoteDataSource
                .deleteCampaign(id.toInt())
                .dataAssertNoErrors.rangers_remove_campaign
                .map { it.campaign.toDbCampaign() }
                .filter { it.id != id }

            db.withTransaction {
                campaignDao.upsertAllCampaigns(updatedData)
                campaignDao.deleteCampaignById(id)
            }
        } else {
            val campaign = campaignDao.getCampaignById(id)!!
            val currentTime = getCurrentDateTime()
            val decks = deckDao.getDecksById(campaign.latestDecks.jsonObject.keys.toList())

            db.withTransaction {
                if (campaign.previousCampaignId != null) {
                    val previousCampaign = campaignDao.getCampaignById(campaign.previousCampaignId)!!
                    campaignDao.updateCampaign(
                        previousCampaign.copy(
                            latestDecks = campaign.latestDecks,
                            updatedAt = currentTime,
                            nextCampaignId = null
                        )
                    )
                    deckDao.upsertAllDecks(decks.map {
                        it.copy(
                            updatedAt = currentTime,
                            campaignId = campaign.previousCampaignId,
                        )
                    })
                } else {
                    deckDao.upsertAllDecks(decks.map {
                        it.copy(
                            updatedAt = currentTime,
                            campaignId = null,
                            campaignName = null
                        )
                    })
                }
                campaignDao.deleteCampaignById(id)
            }
        }
    }

    override suspend fun upsertChallengeDeck(campaignId: String, challengeDeckIds: List<Int>) =
        campaignDao.upsertChallengeDeck(
            ChallengeDeck(
                campaignId,
                buildJsonArray {
                    challengeDeckIds.forEach { add(it) }
                }
            )
        )

    override fun getCampaignChallengeDeckFlowById(campaignId: String) =
        campaignDao.getCampaignChallengeDeckFlowById(campaignId).map {
            it?.jsonArray?.map { id -> id.jsonPrimitive.content.toInt() } ?: emptyList()
        }

    override fun startSubscription(campaignId: String): Flow<Result<Unit>> {
        val id = campaignId.toIntOrNull() ?: return flowOf(Result.success(Unit))
        return campaignsRemoteDataSource.startSubscription(id)
            .onEach { response ->
                response.dataAssertNoErrors.campaign?.campaign?.let { remoteCampaign ->
                    val localCampaign = campaignDao.getCampaignById(campaignId)
                    localCampaign?.let {
                        val dbCampaign = remoteCampaign.toDbCampaign()
                        if (localCampaign.updatedAt != dbCampaign.updatedAt)
                            campaignDao.updateCampaign(dbCampaign)
                    }
                }
            }
            .map { Result.success(Unit) }
            .catch { throwable -> emit(Result.failure(throwable)) }
    }
}