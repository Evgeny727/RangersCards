package com.rangerscards.ui.campaigns

import android.content.Context
import android.net.ConnectivityManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.google.firebase.auth.FirebaseUser
import com.rangerscards.CreateCampaignMutation
import com.rangerscards.GetCampaignQuery
import com.rangerscards.GetMyCampaignsQuery
import com.rangerscards.TransferCampaignMutation
import com.rangerscards.data.local.campaign.Campaign
import com.rangerscards.data.local.campaign.CampaignListItemProjection
import com.rangerscards.data.local.deck.Deck
import com.rangerscards.domain.repository.DeckRepository
import com.rangerscards.domain.TimestampNormilizer
import com.rangerscards.ui.decks.getCurrentDateTime
import com.rangerscards.ui.decks.toDeck
import com.rangerscards.ui.settings.UserUIState
import com.rangerscards.ui.settings.performFirebaseOperationWithRetry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class CampaignsViewModel(
    private val apolloClient: ApolloClient,
    private val campaignsRepository: CampaignsRepository,
    private val deckRepository: DeckRepository,
) : ViewModel() {
    
    private val _campaignIdToOpen = MutableStateFlow("")
    val campaignIdToOpen: StateFlow<String> = _campaignIdToOpen.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Holds the current search term entered by the user.
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun getAllNetworkCampaigns(user: FirebaseUser?, context: Context) {
        viewModelScope.launch {
            _isRefreshing.update { true }
            if (isConnected(context) && user != null) {
                var token: String? = ""
                val result = performFirebaseOperationWithRetry {
                    token = user.getIdToken(true).await().token
                }
                if (result != null) {
                    val response = apolloClient.query(GetMyCampaignsQuery(user.uid))
                        .addHttpHeader("Authorization", "Bearer $token")
                        .fetchPolicy(FetchPolicy.NetworkOnly).execute()
                    if (response.data != null) {
                        if (response.data!!.campaigns.isEmpty()) campaignsRepository.syncCampaigns(emptyList())
                        else {
                            campaignsRepository.syncCampaigns(response.data!!.campaigns.toCampaigns(true))
                            val decks = response.data!!.campaigns.flatMap { campaign ->
                                campaign.campaign!!.campaign.latest_decks.map { deck ->
                                    deck.deck!!.deck.toDeck(true)
                                }
                            }
                            campaignsRepository.insertDecks(decks)
                        }
                    }
                }
            } else campaignsRepository.deleteAllUploadedCampaigns()
        }.invokeOnCompletion { _isRefreshing.update { false } }
    }

    fun isConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork) != null
    }

    // Exposes the paginated search results as PagingData.
    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults: Flow<PagingData<CampaignListItemProjection>> =
        _searchQuery.flatMapLatest { query ->
            // When the search query or include flag changes, perform a new search.
            if (query.trim().isEmpty()) {
                campaignsRepository.getAllCampaigns().catch { throwable ->
                    // Log the error.
                    throwable.printStackTrace()
                    // Return an empty PagingData on error so that the flow continues.
                    emit(PagingData.empty())
                }
            } else {
                campaignsRepository.searchCampaigns(query.trim()).catch { throwable ->
                    // Log the error.
                    throwable.printStackTrace()
                    // Return an empty PagingData on error so that the flow continues.
                    emit(PagingData.empty())
                }
            }
        }.cachedIn(viewModelScope)

    fun getTransferCampaigns(cycleId: String, currentUserId: FirebaseUser?) =
        campaignsRepository.getAllCampaignsForTransfer(cycleId, currentUserId?.uid ?: "")

    fun getRolesImages(ids: List<String>): Flow<List<String>> =
        campaignsRepository.getRolesImages(ids).map { rolesList ->
            // Create a map from id to RoleCardProjection
            val itemById = rolesList.associateBy { it.id }
            // Map the list of ids to the corresponding real image URLs.
            ids.map { id -> itemById[id]?.realImageSrc.orEmpty() }
        }

    /**
     * Called when the user enters a new search term.
     */
    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.update {
            newQuery
        }
    }

    fun clearSearchQuery() {
        _searchQuery.update { "" }
    }

    @OptIn(ExperimentalUuidApi::class)
    suspend fun createCampaign(
        name: String,
        cycleId: String,
        currentLocation: String,
        isUploading: Boolean,
        user: UserUIState,
        transferCampaignId: String,
        expansions: List<String>
    ) {
        if (transferCampaignId.isEmpty()) {
            if (isUploading) {
                val token = user.currentUser!!.getIdToken(true).await().token
                val newCampaign = apolloClient.mutation(
                    CreateCampaignMutation(
                        name = name,
                        cycleId = cycleId,
                        currentLocation = currentLocation,
                        expansions = buildJsonArray { expansions.forEach { add(it) } },
                        calendar = JsonArray(emptyList()),
                    )
                ).addHttpHeader("Authorization", "Bearer $token").execute()
                if (newCampaign.data != null) {
                    campaignsRepository.insertCampaign(newCampaign.data!!.campaign!!.campaign.toCampaign(true))
                    _campaignIdToOpen.update { newCampaign.data!!.campaign!!.campaign.id.toString() }
                }
            } else {
                val uuid = Uuid.random().toString()
                campaignsRepository.insertCampaign(createLocalCampaign(
                    id = uuid,
                    name = name,
                    cycleId = cycleId,
                    currentLocation = currentLocation,
                    expansions = expansions
                ))
                _campaignIdToOpen.update { uuid }
            }
        }
        else {
            val isUploaded = transferCampaignId.toIntOrNull() != null
            if (isUploaded) {
                val token = user.currentUser!!.getIdToken(true).await().token
                val newCampaign = apolloClient.mutation(
                    TransferCampaignMutation(
                        campaignId = transferCampaignId.toInt(),
                        cycleId = cycleId,
                        currentLocation = currentLocation,
                    )
                ).addHttpHeader("Authorization", "Bearer $token").execute()
                if (newCampaign.data != null) {
                    val oldCampaign = apolloClient.query(
                        GetCampaignQuery(campaignId = transferCampaignId.toInt())
                    ).addHttpHeader("Authorization", "Bearer $token")
                        .fetchPolicy(FetchPolicy.NetworkOnly).execute()
                    campaignsRepository.upsertCampaigns(
                        listOf(
                            newCampaign.data!!.campaign[0].campaign.toCampaign(true),
                            oldCampaign.data!!.campaign!!.campaign.toCampaign(true)
                        )
                    )
                    _campaignIdToOpen.update { newCampaign.data!!.campaign.first().campaign.id.toString() }
                }
            } else {
                val uuid = Uuid.random().toString()
                val previousCampaign = campaignsRepository.getCampaignById(transferCampaignId)
                campaignsRepository.upsertCampaigns(listOf(
                    previousCampaign.copy(
                        latestDecks = JsonObject(emptyMap()),
                        updatedAt = getCurrentDateTime(),
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
                        createdAt = getCurrentDateTime(),
                        updatedAt = getCurrentDateTime(),
                        previousCampaignId = previousCampaign.id
                    )
                ))
                val decks: MutableList<Deck> = mutableListOf()
                for (deck in previousCampaign.latestDecks.jsonObject) {
                    var deckDb = deckRepository.getDeck(deck.key)
                    if (deckDb != null) {
                        decks.add(deckDb.copy(
                            updatedAt = getCurrentDateTime(),
                            campaignId = uuid,
                        ))
                        while (deckDb!!.previousId != null) {
                            deckDb = deckRepository.getDeck(deckDb.previousId)
                            decks.add(deckDb!!.copy(
                                updatedAt = getCurrentDateTime(),
                                campaignId = uuid,
                            ))
                        }
                    }
                }
                deckRepository.upsertDecks(decks)
                _campaignIdToOpen.update { uuid }
            }
        }
    }

    private fun createLocalCampaign(
        id: String,
        name: String,
        cycleId: String,
        currentLocation: String,
        expansions: List<String>
    ): Campaign {
        return Campaign(
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
            createdAt = getCurrentDateTime(),
            updatedAt = getCurrentDateTime(),
            expansions = buildJsonArray { expansions.forEach { add(it) } },
            latestDecks = JsonObject(emptyMap()),
            access = JsonObject(emptyMap()),
            nextCampaignId = null,
            previousCampaignId = null
        )
    }
}

/**
 * Extension function to convert [com.rangerscards.fragment.Campaign] to [Campaign]
 */
fun com.rangerscards.fragment.Campaign.toCampaign(uploaded: Boolean): Campaign {
    val campaign = this
    return Campaign(
        id = this.id.toString(),
        uploaded = uploaded,
        userId = this.user_id,
        name = this.name,
        notes = this.notes,
        day = this.day,
        extendedCalendar = this.extended_calendar,
        cycleId = this.cycle_id,
        currentLocation = this.current_location!!,
        currentPathTerrain = this.current_path_terrain,
        missions = this.missions,
        events = this.events,
        rewards = this.rewards,
        removed = this.removed,
        history = this.history,
        calendar = this.calendar,
        createdAt = TimestampNormilizer.fixFraction(this.created_at),
        updatedAt = TimestampNormilizer.fixFraction(this.updated_at),
        expansions = this.expansions ?: JsonArray(emptyList()),
        latestDecks = buildJsonObject { campaign.latest_decks.forEach {
            put(it.deck!!.deck.id.toString(), buildJsonArray {
                add(it.deck.deck.name)
                add(it.deck.deck.meta)
                add(buildJsonObject {
                    put(it.deck.deck.user_id, it.deck.deck.user.userInfo.handle)
                })
            })
        } },
        access = buildJsonObject { campaign.access.forEach {
            put(it.user!!.id, it.user.userInfo.handle)
        } },
        nextCampaignId = this.next_campaign_id?.toString(),
        previousCampaignId = this.previous_campaign?.id?.toString()
    )
}

/**
 * Extension function to convert list of [GetMyCampaignsQuery.Campaign] to list of [Campaign]
 */
fun List<GetMyCampaignsQuery.Campaign>.toCampaigns(uploaded: Boolean): List<Campaign> {
    return this.map {
        it.campaign!!.campaign.toCampaign(uploaded)
    }
}