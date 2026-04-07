package com.rangerscards.ui.campaigns

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.google.firebase.auth.FirebaseUser
import com.rangerscards.AddCampaignEventMutation
import com.rangerscards.AddCampaignMissionMutation
import com.rangerscards.AddCampaignRemovedMutation
import com.rangerscards.AddFriendToCampaignMutation
import com.rangerscards.CampaignSubscription
import com.rangerscards.CampaignTravelMutation
import com.rangerscards.CampaignUndoTravelMutation
import com.rangerscards.CreateCampaignMutation
import com.rangerscards.DeleteCampaignMutation
import com.rangerscards.ExtendCampaignMutation
import com.rangerscards.GetCampaignQuery
import com.rangerscards.GetDeckQuery
import com.rangerscards.GetMyDecksQuery
import com.rangerscards.LeaveCampaignMutation
import com.rangerscards.R
import com.rangerscards.RemoveDeckCampaignMutation
import com.rangerscards.RemoveFriendFromCampaignMutation
import com.rangerscards.SetCampaignCalendarMutation
import com.rangerscards.SetCampaignDayMutation
import com.rangerscards.SetCampaignMissionsMutation
import com.rangerscards.SetCampaignTitleMutation
import com.rangerscards.SetDeckCampaignMutation
import com.rangerscards.UpdateCampaignEventsMutation
import com.rangerscards.UpdateCampaignExpansionsMutation
import com.rangerscards.UpdateCampaignRemovedMutation
import com.rangerscards.UpdateCampaignRewardsMutation
import com.rangerscards.UpdateUploadedMutation
import com.rangerscards.CurrentChallengeDeck
import com.rangerscards.data.local.campaign.Campaign
import com.rangerscards.data.local.card.CardListItemProjection
import com.rangerscards.data.local.card.FullCardProjection
import com.rangerscards.data.local.deck.Deck
import com.rangerscards.data.local.deck.RoleCardProjection
import com.rangerscards.domain.repository.CampaignsRepository
import com.rangerscards.objects.CampaignMaps
import com.rangerscards.objects.Path
import com.rangerscards.objects.Weather
import com.rangerscards.ui.decks.getCurrentDateTime
import com.rangerscards.ui.decks.toDeck
import com.rangerscards.ui.decks.toDecks
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

data class DayInfo(
    val guides: List<String>,
    @DrawableRes val moonIconId: Int
)

class CampaignViewModel(
    private val apolloClient: ApolloClient,
    private val campaignsRepository: CampaignsRepository,
    private val deckRepository: DeckRepository,
) : ViewModel() {

    fun getCampaignById(id: String) = campaignsRepository.getCampaignFlowById(id)

    fun getCampaignChallengeDeckIds(id: String) =
        campaignsRepository.getCampaignChallengeDeckFlowById(id)

    var isSubscriptionStarted = MutableStateFlow(false)
        private set

    private val _campaign = MutableStateFlow<CampaignState?>(null)
    val campaign: StateFlow<CampaignState?> = _campaign.asStateFlow()

    val uploadedCampaignIdToOpen = MutableStateFlow<String?>(null)

    val friendDeckIdToOpen = MutableStateFlow<String?>(null)

    var  currentChallengeDeck: CurrentChallengeDeck? = null
        private set

    fun setChallengeDeck(ids: JsonElement) {
        val listOfIds = ids.jsonArray.map { it.jsonPrimitive.content.toInt() }
        currentChallengeDeck = CurrentChallengeDeck(listOfIds)
    }

    fun startSubscription(campaignId: String) {
        viewModelScope.launch {
            try {
                // Convert the Apollo subscription call to a Flow
                apolloClient.subscription(CampaignSubscription(campaignId.toInt()))
                    .toFlow()
                    .collect { response ->
                        // Check for errors or handle the data
                        if (response.hasErrors()) {
                            // Handle error case
                            Log.d("SubscriptionErrors", response.errors.toString())
                        } else {
                            val data = response.data
                            if (data != null) {
                                isSubscriptionStarted.update { true }
                                campaignsRepository.updateCampaign(data.campaign!!.campaign.toCampaign(true))
                            }
                        }
                    }
            } catch (e: Exception) {
                // Handle cancellation or other exceptions
                // For instance, log the error or inform the user
                isSubscriptionStarted.update { false }
                Log.e("SubscriptionError", e.message.toString())
            }
        }
    }

    fun parseCampaign(campaign: Campaign) {
        _campaign.update {
            campaign.toCampaignState()
        }
    }

    suspend fun updateCampaignName(
        campaignId: String,
        newName: String,
        uploaded: Boolean,
        user: FirebaseUser?
    ) {
        if (uploaded) {
            val token = user!!.getIdToken(true).await().token
            apolloClient.mutation(
                SetCampaignTitleMutation(
                name = newName,
                campaignId = campaignId.toInt(),
            )
            ).addHttpHeader("Authorization", "Bearer $token").execute()
        } else {
            val campaign = campaignsRepository.getCampaignById(campaignId)
            campaignsRepository.updateCampaign(campaign.copy(name = newName,
                updatedAt = getCurrentDateTime()))
        }
    }

    // This function creates an extended weather list when needed.
    private fun getExtendedWeatherList(expansions: List<String>): List<Weather> {
        val campaignValue = campaign.value ?: return emptyList()
        val weathers = CampaignMaps.weather(campaignValue.cycleId)

        if (!campaignValue.extendedCalendar) return weathers

        val weatherList = weathers.toMutableList()

        if (expansions.isEmpty()) {
            // no expansions -> add shifted copy of the base weathers
            weatherList += weathers.map { original ->
                original.copy(start = original.start + 30, end = original.end + 30)
            }
            return weatherList.toList()
        }

        // Find the first expansion that yields a non-empty list and append it (only one)
        val firstNonEmpty = expansions
            .asSequence()
            .map { CampaignMaps.expansionsWeather(it) }
            .firstOrNull { it.isNotEmpty() }

        if (firstNonEmpty != null) {
            weatherList += firstNonEmpty
        }

        return weatherList.toList()
    }

    // This function groups days by the corresponding Weather.
    // For extendedCalendar, days 31-60 mirror days 1-30.
    fun groupDaysByWeather(): Map<Weather, Map<Int, DayInfo>> {
        val campaign = campaign.value!!
        val weathers = getExtendedWeatherList(campaign.expansions)
        val guidesMap = campaign.calendar.toMutableMap()
        val starterGuides = CampaignMaps.fixedGuideEntries[campaign.cycleId]!!
        for ((key, value) in starterGuides) {
            // Check if the key exists in the first map
            if (guidesMap.containsKey(key)) {
                // If yes, merge the lists (concatenate the values)
                // Using the plus operator to concatenate two lists
                guidesMap[key] = value + guidesMap[key]!!
            } else {
                // If the key does not exist, add it to the first map
                guidesMap[key] = value
            }
        }
        val iconsId = CampaignMaps.moonIconsMap()
        // Determine the maximum day based on calendar mode
        val maxDay = if (campaign.extendedCalendar) 60 else 30
        val result = mutableMapOf<Weather, MutableList<Int>>()
        val dayInfoMap = mutableMapOf<Int, DayInfo>()
        // Iterate over the days in the defined range.
        for (day in 1..maxDay) {
            val weatherForDay = weathers.firstOrNull { day in it.start..it.end }
            if (weatherForDay != null) {
                result.getOrPut(weatherForDay) { mutableListOf() }.add(day)
            }
            dayInfoMap[day] = DayInfo(
                guidesMap[day] ?: emptyList(),
                if (day > 30) iconsId[day - 30]!! else iconsId[day]!!
            )
        }
        return result.mapValues { (_, days) ->
            days.associateWith { day -> dayInfoMap[day]!! }
        }
    }

    suspend fun setCampaignCalendar(day: Int, guides: List<String>, user: FirebaseUser?) {
        val campaign = campaign.value!!
        val map: MutableMap<Int, List<String>> = campaign.calendar.toMutableMap()
        if (map.containsKey(day)) {
            if (guides.isEmpty()) map.remove(day)
            else map[day] = guides
        } else {
            if (guides.isNotEmpty()) map[day] = guides
        }
        val newCalendar = buildJsonArray { map.forEach { add(buildJsonObject {
            put("day", it.key)
            put("guides", buildJsonArray { it.value.forEach { guide -> add(guide) } })
        }) } }
        if (campaign.uploaded) {
            val token = user!!.getIdToken(true).await().token
            apolloClient.mutation(
                SetCampaignCalendarMutation(
                    campaignId = campaign.id.toInt(),
                    calendar = newCalendar,
                )
            ).addHttpHeader("Authorization", "Bearer $token").execute()
        } else {
            val campaignEntry = campaignsRepository.getCampaignById(campaign.id)
            campaignsRepository.updateCampaign(campaignEntry.copy(calendar = newCalendar,
                updatedAt = getCurrentDateTime()))
        }
    }

    /**
     * Computes a list of TravelDay objects based on the campaign history.
     */
    fun buildTravelHistory(history: List<CampaignHistory>): List<CampaignTravelDay> {
        val campaign = campaign.value ?: return emptyList()
        // Group entries by day
        val daysMap = history.groupBy { it.day }
        val result = mutableListOf<CampaignTravelDay>()
        // Determine the starting live location using a constant lookup.
        var liveLocation: String? = CampaignMaps.startingLocations[campaign.cycleId]
        // For each day from 1 to campaign.day, build the travel day object.
        for (day in 1..campaign.currentDay) {
            val travel = daysMap[day] ?: emptyList()
            result.add(
                CampaignTravelDay(
                    day = day,
                    startingLocation = liveLocation,
                    travel = travel
                )
            )
            if (travel.isNotEmpty()) {
                // Update liveLocation to the location from the last entry
                liveLocation = travel.lastOrNull()?.location ?: liveLocation
            }
        }
        return result
    }

    fun getWeatherResId(day: Int): Int {
        val campaign = campaign.value!!
        val weatherList = CampaignMaps.weather(campaign.cycleId)
        return (weatherList.firstOrNull { day in it.start..it.end }
            ?: weatherList.firstOrNull { day in (it.start + 30)..(it.end + 30) })?.nameResId!!
    }

    suspend fun extendCampaign(user: FirebaseUser?) {
        val campaign = campaign.value!!
        if (campaign.uploaded) {
            val token = user!!.getIdToken(true).await().token
            apolloClient.mutation(
                ExtendCampaignMutation(campaignId = campaign.id.toInt())
            ).addHttpHeader("Authorization", "Bearer $token").execute()
        } else {
            val campaignEntry = campaignsRepository.getCampaignById(campaign.id)
            campaignsRepository.updateCampaign(campaignEntry.copy(
                extendedCalendar = true,
                updatedAt = getCurrentDateTime()))
        }
    }

    suspend fun setCampaignDay(user: FirebaseUser?) {
        val campaign = campaign.value!!
        if (campaign.uploaded) {
            val token = user!!.getIdToken(true).await().token
            apolloClient.mutation(
                SetCampaignDayMutation(
                    campaignId = campaign.id.toInt(),
                    day = campaign.currentDay + 1
                )
            ).addHttpHeader("Authorization", "Bearer $token").execute()
        } else {
            val campaignEntry = campaignsRepository.getCampaignById(campaign.id)
            campaignsRepository.updateCampaign(campaignEntry.copy(
                day = campaign.currentDay + 1,
                updatedAt = getCurrentDateTime()))
        }
        reshuffleChallengeDeck()
    }

    suspend fun campaignTravel(
        selectedLocation: String,
        selectedPathTerrain: String,
        isCamping: Boolean,
        user: FirebaseUser?
    ) {
        val campaign = campaign.value!!
        if (campaign.uploaded) {
            val token = user!!.getIdToken(true).await().token
            apolloClient.mutation(
                CampaignTravelMutation(
                    campaignId = campaign.id.toInt(),
                    day = campaign.currentDay + if (isCamping) 1 else 0,
                    currentLocation = selectedLocation,
                    currentPathTerrain = selectedPathTerrain,
                    history = buildJsonObject {
                        put("day", campaign.currentDay)
                        put("camped", isCamping)
                        put("location", selectedLocation)
                        put("path_terrain", selectedPathTerrain)
                    }
                )
            ).addHttpHeader("Authorization", "Bearer $token").execute()
        } else {
            val newHistory = campaign.history + CampaignHistory(
                campaign.currentDay,
                isCamping,
                selectedLocation,
                selectedPathTerrain
            )
            val newHistoryJson = buildJsonArray { newHistory.forEach { add(buildJsonObject {
                put("day", it.day)
                put("camped", it.camped)
                put("location", it.location)
                put("path_terrain", it.pathTerrain)
            }) } }
            val campaignEntry = campaignsRepository.getCampaignById(campaign.id)
            campaignsRepository.updateCampaign(campaignEntry.copy(
                day = campaign.currentDay + if (isCamping) 1 else 0,
                currentLocation = selectedLocation,
                currentPathTerrain = selectedPathTerrain,
                history = newHistoryJson,
                updatedAt = getCurrentDateTime()
            ))
        }
        if (isCamping) reshuffleChallengeDeck()
    }

    suspend fun drawChallengeCard(): Int? {
        val drawCardId = currentChallengeDeck?.draw()
        campaignsRepository.upsertChallengeDeck(
            campaign.value!!.id,
            buildJsonArray { currentChallengeDeck?.getDeckAsList()?.forEach { add(it) } }
            )
        return drawCardId
    }

    fun scoutChallengeCard(): Int? {
        return currentChallengeDeck?.scout()
    }

    suspend fun returnChallengeCardsInAnyOrder(topList: List<Int>, bottomList: List<Int>) {
        val deck = currentChallengeDeck?.getDeckAsList() ?: emptyList()
        val exclude = (topList + bottomList).toSet()
        val middleList = deck.filter { it !in exclude }
        val newList = topList + middleList + bottomList
        campaignsRepository.upsertChallengeDeck(
            campaign.value!!.id,
            buildJsonArray { newList.forEach { add(it) } }
        )
        currentChallengeDeck?.updateDeckWithDifferentOrder(newList)
    }

    fun discardScoutedCards() = currentChallengeDeck?.resetScoutPosition()

    suspend fun reshuffleChallengeDeck() {
        campaignsRepository.upsertChallengeDeck(
            campaign.value!!.id,
            buildJsonArray { currentChallengeDeck?.reshuffle()?.forEach { add(it) } }
        )
    }

    private val _taboo = MutableStateFlow(false)
    private val _packId = MutableStateFlow("core")

    val showAllRewards = MutableStateFlow(false)

    private val _collection = MutableStateFlow(listOf("core"))

    fun getRole(id: String): Flow<RoleCardProjection?> = campaignsRepository.getRole(id, _taboo.value)

    fun setTaboo(taboo: Boolean?) {
        _taboo.update { taboo ?: false }
    }

    fun setPackId(id: String) {
        _packId.update { id }
    }

    fun setShowAllRewards() {
        showAllRewards.update { !it }
    }

    fun setCollection(collection: List<String>) {
        _collection.update { collection + "core" }
    }


    suspend fun removeDeckCampaign(deckId: String, user: FirebaseUser?, updateCampaign: Boolean = true) {
        val campaign = campaign.value!!
        if (campaign.uploaded) {
            val token = user!!.getIdToken(true).await().token
            apolloClient.mutation(
                RemoveDeckCampaignMutation(
                    deckId = deckId.toInt(),
                    campaignId = campaign.id.toInt(),
                )
            ).addHttpHeader("Authorization", "Bearer $token").execute()
            val response = apolloClient.query(GetDeckQuery(deckId.toInt()))
                .addHttpHeader("Authorization", "Bearer $token")
                .fetchPolicy(FetchPolicy.NetworkOnly).execute()
            if (response.data != null) deckRepository.updateDeck(response.data!!.deck!!.deck.toDeck(true))
        } else {
            val decks: MutableList<Deck> = mutableListOf()
            var deck = deckRepository.getDeck(deckId)
            if (deck != null) {
                decks.add(deck.copy(
                    updatedAt = getCurrentDateTime(),
                    campaignId = null,
                    campaignName = null
                ))
                while (deck!!.previousId != null) {
                    deck = deckRepository.getDeck(deck.previousId)
                    decks.add(deck!!.copy(
                        updatedAt = getCurrentDateTime(),
                        campaignId = null,
                        campaignName = null
                    ))
                }
            }
            deckRepository.upsertDecks(decks)
            if (updateCampaign) {
                val campaignEntry = campaignsRepository.getCampaignById(campaign.id)
                campaignsRepository.updateCampaign(campaignEntry.copy(
                    latestDecks = JsonObject(campaignEntry.latestDecks.jsonObject.filterKeys { it != deckId }),
                    updatedAt = getCurrentDateTime()
                ))
            }
        }
    }

    suspend fun addDeckCampaign(deckId: String, user: FirebaseUser?) {
        val campaign = campaign.value!!
        if (campaign.uploaded) {
            val token = user!!.getIdToken(true).await().token
            apolloClient.mutation(
                SetDeckCampaignMutation(
                    deckId = deckId.toInt(),
                    campaignId = campaign.id.toInt(),
                )
            ).addHttpHeader("Authorization", "Bearer $token").execute()
            val response = apolloClient.query(GetDeckQuery(deckId.toInt()))
                .addHttpHeader("Authorization", "Bearer $token")
                .fetchPolicy(FetchPolicy.NetworkOnly).execute()
            if (response.data != null) deckRepository.updateDeck(response.data!!.deck!!.deck.toDeck(true))
        } else {
            val deck = deckRepository.getDeck(deckId)!!
            deckRepository.updateDeck(deck.copy(
                campaignId = campaign.id,
                campaignName = campaign.name,
                campaignRewards = buildJsonArray { campaign.rewards.forEach { add(it) } }
            ))
            val campaignEntry = campaignsRepository.getCampaignById(campaign.id)
            val newDeckJson = buildJsonArray {
                add(deck.name)
                add(deck.meta)
                add(buildJsonObject {
                    put(deck.userId, deck.userHandle)
                })
            }
            campaignsRepository.updateCampaign(campaignEntry.copy(
                latestDecks = JsonObject(campaignEntry.latestDecks.jsonObject + (deckId to newDeckJson)),
                updatedAt = getCurrentDateTime()
            ))
        }
    }

    suspend fun downloadFriendDeck(deckId: String, user: FirebaseUser?) {
        val token = user!!.getIdToken(true).await().token
        val response = apolloClient.query(GetDeckQuery(deckId.toInt()))
            .addHttpHeader("Authorization", "Bearer $token")
            .fetchPolicy(FetchPolicy.NetworkOnly).execute()
        if (response.data != null) {
            deckRepository.upsertDeck(response.data!!.deck!!.deck.toDeck(true))
            friendDeckIdToOpen.update { response.data!!.deck!!.deck.id.toString() }
        }
    }

    suspend fun addFriendToCampaign(user: FirebaseUser?, friendId: String) {
        val campaign = campaign.value!!
        val token = user!!.getIdToken(true).await().token
        apolloClient.mutation(
            AddFriendToCampaignMutation(
                campaignId = campaign.id.toInt(),
                userId = friendId
            )
        ).addHttpHeader("Authorization", "Bearer $token").execute()
    }

    suspend fun removeFriendFromCampaign(user: FirebaseUser?, friendId: String) {
        val campaign = campaign.value!!
        val token = user!!.getIdToken(true).await().token
        apolloClient.mutation(
            RemoveFriendFromCampaignMutation(
                campaignId = campaign.id.toInt(),
                userId = friendId
            )
        ).addHttpHeader("Authorization", "Bearer $token").execute()
    }

    suspend fun uploadCampaign(user: FirebaseUser?) {
        val campaign = campaign.value!!
        val token = user!!.getIdToken(true).await().token
        val uploadedCampaign = apolloClient.mutation(
            CreateCampaignMutation(
                name = campaign.name,
                cycleId = campaign.cycleId,
                currentLocation = campaign.currentLocation,
                expansions = buildJsonArray { campaign.expansions.forEach { add(it) } },
                calendar = buildJsonArray { campaign.calendar.forEach { entry ->
                    add(buildJsonObject {
                        put("day", entry.key)
                        put("guides", buildJsonArray { entry.value.forEach { add(it) } })
                    })
                } }
            )
        ).addHttpHeader("Authorization", "Bearer $token").execute()
        if (uploadedCampaign.data != null) {
            val newCampaignId = uploadedCampaign.data!!.campaign!!.campaign.id
            apolloClient.mutation(
                UpdateUploadedMutation(
                    campaignId = newCampaignId,
                    currentPathTerrain = Optional.present(campaign.currentPathTerrain),
                    day = campaign.currentDay,
                    extendedCalendar = Optional.present(campaign.extendedCalendar),
                    rewards = buildJsonArray { campaign.rewards.forEach { add(it) } },
                    missions = buildJsonArray { campaign.missions.forEach { add(buildJsonObject {
                        put("day", it.day)
                        put("name", it.name)
                        put("checks", buildJsonArray { it.checks.forEach { check -> add(check) } })
                        put("completed", it.completed)
                    }) } },
                    events = buildJsonArray { campaign.events.forEach { add(buildJsonObject {
                        put("event", it.name)
                        put("crossed_out", it.crossedOut)
                        put("marks", it.marks)
                    }) } },
                    removed = buildJsonArray { campaign.removed.forEach { add(buildJsonObject {
                        put("name", it.name)
                        put("set_id", it.setId)
                    }) } },
                    history = buildJsonArray { campaign.history.forEach { add(buildJsonObject {
                        put("day", it.day)
                        put("camped", it.camped)
                        put("location", it.location)
                        put("path_terrain", it.pathTerrain)
                    }) } },
                    calendar = buildJsonArray { campaign.calendar.forEach { add(buildJsonObject {
                        put("day", it.key)
                        put("guides", buildJsonArray { it.value.forEach { guide -> add(guide) } })
                    }) } }
                )
            ).addHttpHeader("Authorization", "Bearer $token").execute()
            val newCampaign = apolloClient.query(GetCampaignQuery(newCampaignId)).fetchPolicy(FetchPolicy.NetworkOnly)
                .addHttpHeader("Authorization", "Bearer $token").execute()
            if (newCampaign.data != null) {
                val uploadedData = newCampaign.data!!.campaign!!.campaign
                campaignsRepository.insertCampaign(uploadedData.toCampaign(true))
                uploadedCampaignIdToOpen.update { uploadedData.id.toString() }
                campaignsRepository.deleteCampaign(campaign.id)
            }
        }
    }

    suspend fun deleteCampaign(user: FirebaseUser?) {
        val campaign = campaign.value!!
        if (campaign.uploaded) {
            val token = user!!.getIdToken(true).await().token
            apolloClient.mutation(
                DeleteCampaignMutation(
                    campaignId = campaign.id.toInt(),
                )
            ).addHttpHeader("Authorization", "Bearer $token").execute()
            val response = apolloClient.query(GetMyDecksQuery(userId = user.uid))
                .addHttpHeader("Authorization", "Bearer $token")
                .fetchPolicy(FetchPolicy.NetworkOnly).execute()
            if (response.data != null) deckRepository.upsertDecks(response.data!!.decks.toDecks(true))
            if (campaign.previousCampaignId != null) {
                val oldCampaign = apolloClient.query(
                    GetCampaignQuery(campaignId = campaign.previousCampaignId.toInt())
                ).addHttpHeader("Authorization", "Bearer $token")
                    .fetchPolicy(FetchPolicy.NetworkOnly).execute()
                campaignsRepository.updateCampaign(oldCampaign.data!!.campaign!!.campaign.toCampaign(true))
            }
            campaignsRepository.deleteCampaign(campaign.id)
        } else {
            if (campaign.previousCampaignId != null) {
                val previousCampaign = campaignsRepository.getCampaignById(campaign.previousCampaignId)
                val currentCampaign = campaignsRepository.getCampaignById(campaign.id)
                campaignsRepository.updateCampaign(
                    previousCampaign.copy(
                        latestDecks = currentCampaign.latestDecks,
                        updatedAt = getCurrentDateTime(),
                        nextCampaignId = null
                    ))
                val decks: MutableList<Deck> = mutableListOf()
                for (deck in currentCampaign.latestDecks.jsonObject) {
                    var deckDb = deckRepository.getDeck(deck.key)
                    if (deckDb != null) {
                        decks.add(deckDb.copy(
                            updatedAt = getCurrentDateTime(),
                            campaignId = currentCampaign.previousCampaignId,
                        ))
                        while (deckDb!!.previousId != null) {
                            deckDb = deckRepository.getDeck(deckDb.previousId)
                            decks.add(deckDb!!.copy(
                                updatedAt = getCurrentDateTime(),
                                campaignId = currentCampaign.previousCampaignId,
                            ))
                        }
                    }
                }
                deckRepository.upsertDecks(decks)
            } else {
                val deckIds = campaign.decks.map { it.id }
                deckIds.forEach {
                    removeDeckCampaign(it, user, false)
                }
            }
            campaignsRepository.deleteCampaign(campaign.id)
        }
    }

    suspend fun leaveCampaign(user: FirebaseUser?) {
        val campaign = campaign.value!!
        val deckIds = campaign.decks.filter { user?.uid == it.userId }.map { it.id }
        val token = user!!.getIdToken(true).await().token
        apolloClient.mutation(
            LeaveCampaignMutation(
                campaignId = campaign.id.toInt(),
                userId = user.uid
            )
        ).addHttpHeader("Authorization", "Bearer $token").execute()
        deckIds.forEach {
            removeDeckCampaign(it, user, false)
        }
        campaignsRepository.deleteCampaign(campaign.id)
    }

    fun checkIfCanUndo(): Boolean {
        val campaign = campaign.value!!
        // Get the last travel record (if any)
        val lastTravel = campaign.history.lastOrNull()
        // Compute whether we can undo an "end day"
        val canUndoEndDay = campaign.currentDay > 1 && (
                lastTravel == null ||
                        (if (lastTravel.camped) lastTravel.day + 1 else lastTravel.day) < campaign.currentDay
                )
        return lastTravel != null || canUndoEndDay
    }

    suspend fun undoTravel(user: FirebaseUser?) {
        val campaign = campaign.value!!
        // Get the last travel record (if any)
        val lastTravel = campaign.history.lastOrNull()
        // Compute whether we can undo an "end day"
        val canUndoEndDay = campaign.currentDay > 1 && (
                lastTravel == null ||
                        (if (lastTravel.camped) lastTravel.day + 1 else lastTravel.day) < campaign.currentDay
                )
        if (canUndoEndDay) {
            if (campaign.uploaded) {
                val token = user!!.getIdToken(true).await().token
                apolloClient.mutation(
                    SetCampaignDayMutation(
                        campaignId = campaign.id.toInt(),
                        day = campaign.currentDay - 1
                    )
                ).addHttpHeader("Authorization", "Bearer $token").execute()
            } else {
                val campaignEntry = campaignsRepository.getCampaignById(campaign.id)
                campaignsRepository.updateCampaign(campaignEntry.copy(
                    day = campaign.currentDay - 1,
                    updatedAt = getCurrentDateTime()
                ))
            }
            reshuffleChallengeDeck()
        } else if (lastTravel != null) {
            var previousLocation = CampaignMaps.startingLocations[campaign.cycleId]!!
            var previousPathTerrain: String? = null
            if (campaign.history.size >= 2) {
                val penultimateEntry = campaign.history[campaign.history.size - 2]
                if (penultimateEntry.location.isNotEmpty()) {
                    previousLocation = penultimateEntry.location
                }
                previousPathTerrain = penultimateEntry.pathTerrain
            }
            // Remove the last entry from history.
            val newHistory = campaign.history.dropLast(1)
            val newHistoryJson = buildJsonArray { newHistory.forEach { add(buildJsonObject {
                put("day", it.day)
                put("camped", it.camped)
                put("location", it.location)
                put("path_terrain", it.pathTerrain)
            }) } }
            // Adjust previous day depending on whether the last travel had 'camped'
            val previousDay = campaign.currentDay - if (lastTravel.camped) 1 else 0
            if (campaign.uploaded) {
                val token = user!!.getIdToken(true).await().token
                apolloClient.mutation(
                    CampaignUndoTravelMutation(
                        campaignId = campaign.id.toInt(),
                        history = newHistoryJson,
                        previousDay = previousDay,
                        previousLocation = previousLocation,
                        previousPathTerrain = Optional.present(previousPathTerrain)
                    )
                ).addHttpHeader("Authorization", "Bearer $token").execute()
            } else {
                val campaignEntry = campaignsRepository.getCampaignById(campaign.id)
                campaignsRepository.updateCampaign(campaignEntry.copy(
                    day = previousDay,
                    history = newHistoryJson,
                    currentLocation = previousLocation,
                    currentPathTerrain = previousPathTerrain,
                    updatedAt = getCurrentDateTime()
                ))
            }
        }
    }



    fun getRewardsCards(): Flow<List<CardListItemProjection>> {
        val filteredCollection = _collection.value.toSet().filter { if (_packId.value == "core") it != "loa" else true }
        val packIds = if (showAllRewards.value) filteredCollection + _packId.value
            else setOf(_packId.value)
        campaign.value?.id
        return campaignsRepository.getRewards(_taboo.value, packIds.toList())
    }

    fun getRewardById(cardCode: String): Flow<FullCardProjection> =
        campaignsRepository.getCardById(cardCode, _taboo.value)

    suspend fun addCampaignReward(id: String, user: FirebaseUser?) {
        val campaign = campaign.value!!
        val newList = campaign.rewards + id
        val newJsonRewards = buildJsonArray { newList.forEach { add(it) } }
        if (campaign.uploaded) {
            val token = user!!.getIdToken(true).await().token
            apolloClient.mutation(
                UpdateCampaignRewardsMutation(
                    campaignId = campaign.id.toInt(),
                    rewards = newJsonRewards
                )
            ).addHttpHeader("Authorization", "Bearer $token").execute()
        } else {
            val campaignEntry = campaignsRepository.getCampaignById(campaign.id)
            campaignsRepository.updateCampaign(campaignEntry.copy(
                rewards = newJsonRewards,
                updatedAt = getCurrentDateTime()
            ))
        }
    }

    suspend fun removeCampaignReward(id: String, user: FirebaseUser?) {
        val campaign = campaign.value!!
        val newList = campaign.rewards.filterNot { it == id }
        val newJsonRewards = buildJsonArray { newList.forEach { add(it) } }
        if (campaign.uploaded) {
            val token = user!!.getIdToken(true).await().token
            apolloClient.mutation(
                UpdateCampaignRewardsMutation(
                    campaignId = campaign.id.toInt(),
                    rewards = newJsonRewards
                )
            ).addHttpHeader("Authorization", "Bearer $token").execute()
        } else {
            val campaignEntry = campaignsRepository.getCampaignById(campaign.id)
            campaignsRepository.updateCampaign(campaignEntry.copy(
                rewards = newJsonRewards,
                updatedAt = getCurrentDateTime()
            ))
        }
    }

    fun getRemovedSetsInfo(): Map<String, Pair<Int?, Int>> {
        val campaign = campaign.value!!
        val maps = CampaignMaps.generalSetsMap() + CampaignMaps.getMapLocations(false)
        val removedSets = mutableMapOf<String, Pair<Int?, Int>>()
        campaign.removed.forEach { removed ->
            val fromPath = Path.fromValue(removed.setId)
            val fromMaps = maps[removed.setId]
            if (fromPath != null) removedSets[removed.setId] = fromPath.iconResId to fromPath.nameResId
            else removedSets[removed.setId] = fromMaps?.iconResId to (fromMaps?.nameResId ?: R.string.current_path_terrain_none)
        }
        return removedSets
    }

    suspend fun updateCampaignRemoved(name: String, user: FirebaseUser?) {
        val campaign = campaign.value!!
        val newList = campaign.removed.filterNot { it.name == name }
        val newJsonRemoved = buildJsonArray { newList.forEach { add(buildJsonObject {
            put("name", it.name)
            put("set_id", it.setId)
        }) } }
        if (campaign.uploaded) {
            val token = user!!.getIdToken(true).await().token
            apolloClient.mutation(
                UpdateCampaignRemovedMutation(
                    campaignId = campaign.id.toInt(),
                    removed = newJsonRemoved
                )
            ).addHttpHeader("Authorization", "Bearer $token").execute()
        } else {
            val campaignEntry = campaignsRepository.getCampaignById(campaign.id)
            campaignsRepository.updateCampaign(campaignEntry.copy(
                removed = newJsonRemoved,
                updatedAt = getCurrentDateTime()
            ))
        }
    }

    fun getAllRemovedSets(): Map<String, Pair<Int?, Int>> {
        val cycleId = campaign.value!!.cycleId
        val maps = CampaignMaps.generalSetsMap(cycleId) + CampaignMaps.getMapLocations(false, cycleId)
        val paths = Path.entries.filter { it.cycles.contains(cycleId) && it.value != "none" }
        val sets = mutableMapOf<String, Pair<Int?, Int>>()
        maps.forEach { (key, value) ->
            sets[key] = value.iconResId to value.nameResId
        }
        paths.forEach {
            sets[it.value] = it.iconResId to it.nameResId
        }
        return sets
    }

    suspend fun addCampaignRemoved(setId: String, name: String, user: FirebaseUser?) {
        val campaign = campaign.value!!
        val newJsonRemoved = buildJsonObject {
            put("name", name)
            put("set_id", setId)
        }
        if (campaign.uploaded) {
            val token = user!!.getIdToken(true).await().token
            apolloClient.mutation(
                AddCampaignRemovedMutation(
                    campaignId = campaign.id.toInt(),
                    removed = newJsonRemoved
                )
            ).addHttpHeader("Authorization", "Bearer $token").execute()
        } else {
            val campaignEntry = campaignsRepository.getCampaignById(campaign.id)
            campaignsRepository.updateCampaign(campaignEntry.copy(
                removed = JsonArray(campaignEntry.removed.jsonArray + newJsonRemoved),
                updatedAt = getCurrentDateTime()
            ))
        }
    }

    suspend fun recordCampaignEvent(name: String, user: FirebaseUser?) {
        val campaign = campaign.value!!
        val newJsonEvent = buildJsonObject {
            put("event", name)
        }
        if (campaign.uploaded) {
            val token = user!!.getIdToken(true).await().token
            apolloClient.mutation(
                AddCampaignEventMutation(
                    campaignId = campaign.id.toInt(),
                    event = newJsonEvent
                )
            ).addHttpHeader("Authorization", "Bearer $token").execute()
        } else {
            val campaignEntry = campaignsRepository.getCampaignById(campaign.id)
            campaignsRepository.updateCampaign(campaignEntry.copy(
                events = JsonArray(campaignEntry.events.jsonArray + newJsonEvent),
                updatedAt = getCurrentDateTime()
            ))
        }
    }

    suspend fun updateCampaignEvents(oldName: String, newName: String, crossedOut: Boolean, marks: Int, user: FirebaseUser?) {
        val campaign = campaign.value!!
        val newEventsList = campaign.events.map { if (it.name == oldName) CampaignEvent(newName, crossedOut, marks) else it }
        val newJsonList = buildJsonArray { newEventsList.forEach { add(buildJsonObject {
            put("event", it.name)
            put("crossed_out", it.crossedOut)
            put("marks", it.marks)
        }) } }
        if (campaign.uploaded) {
            val token = user!!.getIdToken(true).await().token
            apolloClient.mutation(
                UpdateCampaignEventsMutation(
                    campaignId = campaign.id.toInt(),
                    events = newJsonList
                )
            ).addHttpHeader("Authorization", "Bearer $token").execute()
        } else {
            val campaignEntry = campaignsRepository.getCampaignById(campaign.id)
            campaignsRepository.updateCampaign(campaignEntry.copy(
                events = newJsonList,
                updatedAt = getCurrentDateTime()
            ))
        }
    }

    suspend fun addCampaignMission(day: Int, name: String, user: FirebaseUser?) {
        val campaign = campaign.value!!
        val newJsonMission = buildJsonObject {
            put("day", day)
            put("name", name)
        }
        if (campaign.uploaded) {
            val token = user!!.getIdToken(true).await().token
            apolloClient.mutation(
                AddCampaignMissionMutation(
                    campaignId = campaign.id.toInt(),
                    mission = newJsonMission
                )
            ).addHttpHeader("Authorization", "Bearer $token").execute()
        } else {
            val campaignEntry = campaignsRepository.getCampaignById(campaign.id)
            campaignsRepository.updateCampaign(campaignEntry.copy(
                missions = JsonArray(campaignEntry.missions.jsonArray + newJsonMission),
                updatedAt = getCurrentDateTime()
            ))
        }
    }

    suspend fun setCampaignMissions(
        oldName: String,
        newName: String,
        day: Int,
        checks: List<Boolean>,
        completed: Boolean,
        user: FirebaseUser?
    ) {
        val campaign = campaign.value!!
        val newMissionsList = campaign.missions.map { if (it.name == oldName)
            CampaignMission(
                day,
                newName,
                checks,
                completed
            ) else it }
        val newJsonList = buildJsonArray { newMissionsList.forEach { add(buildJsonObject {
            put("day", it.day)
            put("name", it.name)
            put("checks", buildJsonArray { it.checks.forEach { check -> add(check) } })
            put("completed", it.completed)
        }) } }
        if (campaign.uploaded) {
            val token = user!!.getIdToken(true).await().token
            apolloClient.mutation(
                SetCampaignMissionsMutation(
                    campaignId = campaign.id.toInt(),
                    missions = newJsonList
                )
            ).addHttpHeader("Authorization", "Bearer $token").execute()
        } else {
            val campaignEntry = campaignsRepository.getCampaignById(campaign.id)
            campaignsRepository.updateCampaign(campaignEntry.copy(
                missions = newJsonList,
                updatedAt = getCurrentDateTime()
            ))
        }
    }

    suspend fun deleteCampaignMission(name: String, user: FirebaseUser?) {
        val campaign = campaign.value!!
        val newMissionsList = campaign.missions.filterNot { it.name == name }
        val newJsonList = buildJsonArray { newMissionsList.forEach { add(buildJsonObject {
            put("day", it.day)
            put("name", it.name)
            put("checks", buildJsonArray { it.checks.forEach { check -> add(check) } })
            put("completed", it.completed)
        }) } }
        if (campaign.uploaded) {
            val token = user!!.getIdToken(true).await().token
            apolloClient.mutation(
                SetCampaignMissionsMutation(
                    campaignId = campaign.id.toInt(),
                    missions = newJsonList
                )
            ).addHttpHeader("Authorization", "Bearer $token").execute()
        } else {
            val campaignEntry = campaignsRepository.getCampaignById(campaign.id)
            campaignsRepository.updateCampaign(campaignEntry.copy(
                missions = newJsonList,
                updatedAt = getCurrentDateTime()
            ))
        }
    }

    suspend fun updateCampaignExpansions(expansions: List<String>, user: FirebaseUser?) {
        val campaign = campaign.value!!
        if (expansions.toSet() == campaign.expansions.toSet()) return
        val newJsonExpansions = buildJsonArray { expansions.forEach { add(it) } }
        if (campaign.uploaded) {
            val token = user!!.getIdToken(true).await().token
            apolloClient.mutation(
                UpdateCampaignExpansionsMutation(
                    campaignId = campaign.id.toInt(),
                    expansions = newJsonExpansions
                )
            ).addHttpHeader("Authorization", "Bearer $token").execute()
        } else {
            val campaignEntry = campaignsRepository.getCampaignById(campaign.id)
            campaignsRepository.updateCampaign(campaignEntry.copy(
                expansions = newJsonExpansions,
                updatedAt = getCurrentDateTime()
            ))
        }
    }
}

fun Campaign.toCampaignState(): CampaignState {
    return CampaignState(
        id = this.id,
        uploaded = this.uploaded,
        userId = this.userId,
        name = this.name,
        currentDay = this.day,
        extendedCalendar = this.extendedCalendar ?: false,
        cycleId = this.cycleId,
        currentLocation = this.currentLocation,
        currentPathTerrain = this.currentPathTerrain,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        missions = this.missions.jsonArray.map { element ->
            val value = element.jsonObject
            CampaignMission(
                value["day"]!!.jsonPrimitive.content.toInt(),
                value["name"]!!.jsonPrimitive.content,
                value["checks"]?.jsonArray?.map { it.jsonPrimitive.content.toBoolean() }
                    ?: listOf(false, false, false),
                value["completed"]?.jsonPrimitive?.content.toBoolean()
            )
        },
        events = this.events.jsonArray.map { element ->
            val value = element.jsonObject
            CampaignEvent(
                value["event"]!!.jsonPrimitive.content,
                value["crossed_out"]?.jsonPrimitive?.content.toBoolean(),
                value["marks"]?.jsonPrimitive?.content?.toInt() ?: 0
            )
        },
        rewards = this.rewards.jsonArray.map { it.jsonPrimitive.content },
        removed = this.removed.jsonArray.map { element ->
            val value = element.jsonObject
            CampaignRemoved(
                value["name"]!!.jsonPrimitive.content,
                value["set_id"]?.jsonPrimitive?.content ?: ""
            )
        },
        history = this.history.jsonArray.map { element ->
            val value = element.jsonObject
            CampaignHistory(
                value["day"]!!.jsonPrimitive.content.toInt(),
                value["camped"]!!.jsonPrimitive.content.toBoolean(),
                value["location"]!!.jsonPrimitive.content,
                value["path_terrain"]!!.jsonPrimitive.content
            )
        },
        calendar = this.calendar.jsonArray.associate { element ->
            val value = element.jsonObject
            value["day"]!!.jsonPrimitive.content.toInt() to value["guides"]!!.jsonArray.map { it.jsonPrimitive.content }
        },
        expansions = this.expansions.jsonArray.map { it.jsonPrimitive.content },
        decks = this.latestDecks.jsonObject.map {
            val value = it.value.jsonArray
            val meta = value[1].jsonObject
            val user = value[2].jsonObject
            CampaignDeck(
                it.key,
                value[0].jsonPrimitive.content,
                meta["role"]?.jsonPrimitive?.content ?: "",
                meta,
                user.keys.first(),
                user.values.first().jsonPrimitive.content
            )
        },
        access = this.access.jsonObject.mapValues { it.value.jsonPrimitive.content },
        previousCampaignId = this.previousCampaignId,
        nextCampaignId = this.nextCampaignId
    )
}