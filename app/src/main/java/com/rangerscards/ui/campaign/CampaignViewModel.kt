package com.rangerscards.ui.campaign

import androidx.annotation.DrawableRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rangerscards.CurrentChallengeDeck
import com.rangerscards.R
import com.rangerscards.UiErrorState
import com.rangerscards.domain.exceptions.UploadingCampaignWithDecksException
import com.rangerscards.domain.model.Campaign
import com.rangerscards.domain.model.CampaignCalendar
import com.rangerscards.domain.model.CampaignEvent
import com.rangerscards.domain.model.CampaignHistory
import com.rangerscards.domain.model.CampaignMission
import com.rangerscards.domain.model.CampaignNote
import com.rangerscards.domain.model.CampaignRemoved
import com.rangerscards.domain.model.CampaignTravelDay
import com.rangerscards.domain.model.CardListItem
import com.rangerscards.domain.model.DeckCampaignInfo
import com.rangerscards.domain.model.FullCard
import com.rangerscards.domain.model.RoleCard
import com.rangerscards.domain.model.UserSettings
import com.rangerscards.domain.repository.CampaignsRepository
import com.rangerscards.domain.repository.CardsRepository
import com.rangerscards.domain.repository.DecksRepository
import com.rangerscards.domain.repository.RemoteUpdateAction
import com.rangerscards.domain.repository.UserPreferencesRepository
import com.rangerscards.domain.usecase.GetCampaignRewardsUseCase
import com.rangerscards.objects.CampaignMaps
import com.rangerscards.objects.Path
import com.rangerscards.objects.Weather
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DayInfo(
    val guides: List<String>,
    @DrawableRes val moonIconId: Int
)

data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
)

sealed interface CampaignUiState {
    object Idle : CampaignUiState
    object Loading : CampaignUiState
    data class Deleted(val campaignId: String?) : CampaignUiState
    data class FriendDeckDownloaded(val deckId: String) : CampaignUiState
    data class CampaignUploaded(val campaignId: String) : CampaignUiState
}

@HiltViewModel
class CampaignViewModel @Inject constructor(
    private val campaignsRepository: CampaignsRepository,
    private val decksRepository: DecksRepository,
    private val cardsRepository: CardsRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val getCampaignRewardsUseCase: GetCampaignRewardsUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val campaignId: String = checkNotNull(savedStateHandle["campaignId"])

    val campaign: StateFlow<Campaign?> =
        campaignsRepository.getCampaignFlowById(campaignId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )

    private var _campaignUiState = MutableStateFlow<CampaignUiState>(CampaignUiState.Idle)
    val campaignUiState: StateFlow<CampaignUiState> = _campaignUiState.asStateFlow()

    val isViewOnly = campaign.map { it?.nextCampaignId != null }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun resetCampaignUiState() {
        _campaignUiState.value = CampaignUiState.Idle
    }

    private val _events = MutableSharedFlow<UiErrorState>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val events: SharedFlow<UiErrorState> = _events

    private fun emitError(throwable: Throwable) {
        _events.tryEmit(UiErrorState(throwable))
    }

    private var _currentChallengeDeck = MutableStateFlow<CurrentChallengeDeck?>(null)
    val currentChallengeDeck: StateFlow<CurrentChallengeDeck?> = _currentChallengeDeck.asStateFlow()

    init {
        startCampaignSubscription()
        initializeChallengeDeck()
    }

    fun startCampaignSubscription() {
        viewModelScope.launch {
            campaignsRepository.startSubscription(campaignId).collect { response ->
                response.onFailure { emitError(it) }
            }
        }
    }

    fun initializeChallengeDeck() {
        viewModelScope.launch {
            val ids  = campaignsRepository.getCampaignChallengeDeckFlowById(campaignId).firstOrNull()
            _currentChallengeDeck.value = CurrentChallengeDeck(ids ?: emptyList())
        }
    }



    private var _rewardsQuery = MutableStateFlow("")
    val rewardsQuery: StateFlow<String> = _rewardsQuery.asStateFlow()

    fun onRewardsQueryChange(query: String) {
        _rewardsQuery.value = query
    }

    fun onRewardsQueryClear() {
        _rewardsQuery.value = ""
    }

    val isCampaignMissionsOnlyActive = userPreferencesRepository.showOnlyActiveMissions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun saveCampaignMissionsPreference(value: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.saveCampaignMissionsPreference(value)
        }
    }

    fun updateCampaignName(newName: String) {
        viewModelScope.launch {
            val campaign = campaign.value
            campaign?.let {
                if (campaign.uploaded) _campaignUiState.value = CampaignUiState.Loading
                if (newName != campaign.name) campaignsRepository.updateCampaign(
                    campaign.copy(name = newName),
                    RemoteUpdateAction.SET_TITLE
                ).onFailure { emitError(it) }
                if (campaign.uploaded) _campaignUiState.value = CampaignUiState.Idle
            }
        }
    }

    // This function creates an extended weather list when needed.
    private fun getExtendedWeatherList(campaign: Campaign): List<Weather> {
        val weathers = CampaignMaps.weather(campaign.cycleId)

        if (!campaign.extendedCalendar) return weathers

        val weatherList = weathers.toMutableList()

        if (campaign.expansions.isEmpty()) {
            // no expansions -> add shifted copy of the base weathers
            weatherList += weathers.map { original ->
                original.copy(start = original.start + 30, end = original.end + 30)
            }
            return weatherList.toList()
        }

        // Find the first expansion that yields a non-empty list and append it (only one)
        val firstNonEmpty = campaign.expansions
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
    fun groupDaysByWeather(): ImmutableMap<Weather, ImmutableMap<Int, DayInfo>> {
        val campaign = campaign.value
        if (campaign == null) return persistentMapOf()
        val weathers = getExtendedWeatherList(campaign)
        val guidesMap = campaign.calendar.associate { it.day to it.guides }.toMutableMap()
        val starterGuides = CampaignMaps.fixedGuideEntries[campaign.cycleId]!!
        for ((key, value) in starterGuides) {
            // Check if the key exists in the first map
            if (guidesMap.containsKey(key)) {
                // If yes, merge the lists (concatenate the values)
                // Using the plus operator to concatenate two lists
                guidesMap[key] = (value + guidesMap[key]!!).toImmutableList()
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
            days.associateWith { day -> dayInfoMap[day]!! }.toImmutableMap()
        }.toImmutableMap()
    }

    fun setCampaignCalendar(day: Int, guides: List<String>) {
        viewModelScope.launch {
            val campaign = campaign.value
            campaign?.let {
                if (campaign.uploaded) _campaignUiState.value = CampaignUiState.Loading
                val map: MutableMap<Int, List<String>> = campaign.calendar
                    .associate { it.day to it.guides }.toMutableMap()
                if (map.containsKey(day)) {
                    if (guides.isEmpty()) map.remove(day)
                    else map[day] = guides
                } else {
                    if (guides.isNotEmpty()) map[day] = guides
                }
                val newCalendar = map.map { (key, value) ->
                    CampaignCalendar(key, value.toImmutableList())
                }.toImmutableList()
                campaignsRepository.updateCampaign(
                    campaign.copy(calendar = newCalendar),
                    RemoteUpdateAction.SET_CALENDAR
                ).onFailure { emitError(it) }
                _campaignUiState.value = CampaignUiState.Idle
            }
        }
    }

    /**
     * Computes a list of TravelDay objects based on the campaign history.
     */
    fun buildTravelHistory(history: List<CampaignHistory>): ImmutableList<CampaignTravelDay> {
        val campaign = campaign.value ?: return persistentListOf()
        // Group entries by day
        val daysMap = history.groupBy { it.day }
        val result = mutableListOf<CampaignTravelDay>()
        // Determine the starting live location using a constant lookup.
        var liveLocation: String? = CampaignMaps.startingLocations[campaign.cycleId]
        // For each day from 1 to campaign.day, build the travel day object.
        for (day in 1..campaign.currentDay) {
            val travel = daysMap[day] ?: emptyList()
            result.add(CampaignTravelDay(
                day = day,
                startingLocation = liveLocation,
                travel = travel.toImmutableList()
            ))
            if (travel.isNotEmpty()) {
                // Update liveLocation to the location from the last entry
                liveLocation = travel.lastOrNull()?.location ?: liveLocation
            }
        }
        return result.toImmutableList()
    }

    fun getWeatherResId(day: Int): Int {
        val campaign = campaign.value
        val weatherList = CampaignMaps.weather(campaign?.cycleId ?: "core")
        return (weatherList.firstOrNull { day in it.start..it.end }
            ?: weatherList.firstOrNull { day in (it.start + 30)..(it.end + 30) })?.nameResId ?: R.string.text_none
    }

    fun extendCampaign() {
        viewModelScope.launch {
            val campaign = campaign.value
            campaign?.let {
                if (campaign.uploaded) _campaignUiState.value = CampaignUiState.Loading
                campaignsRepository.updateCampaign(
                    campaign.copy(extendedCalendar = true),
                    RemoteUpdateAction.EXTEND
                ).onFailure { emitError(it) }
                _campaignUiState.value = CampaignUiState.Idle
            }
        }
    }

    fun setCampaignDay() {
        viewModelScope.launch {
            val campaign = campaign.value
            campaign?.let {
                if (campaign.uploaded) _campaignUiState.value = CampaignUiState.Loading
                reshuffleChallengeDeck()
                campaignsRepository.updateCampaign(
                    campaign.copy(currentDay = campaign.currentDay + 1),
                    RemoteUpdateAction.SET_DAY
                ).onFailure { emitError(it) }
                _campaignUiState.value = CampaignUiState.Idle
            }
        }
    }

    fun campaignTravel(
        selectedLocation: String,
        selectedPathTerrain: String,
        isCamping: Boolean,
    ) {
        viewModelScope.launch {
            val campaign = campaign.value
            campaign?.let {
                if (campaign.uploaded) _campaignUiState.value = CampaignUiState.Loading
                val newHistory = campaign.history + CampaignHistory(
                    campaign.currentDay,
                    isCamping,
                    selectedLocation,
                    selectedPathTerrain
                )
                val newCampaign = campaign.copy(
                    currentDay = campaign.currentDay + if (isCamping) 1 else 0,
                    currentLocation = selectedLocation,
                    currentPathTerrain = selectedPathTerrain,
                    history = newHistory.toImmutableList()
                )
                if (isCamping) reshuffleChallengeDeck()
                campaignsRepository.updateCampaign(
                    newCampaign,
                    RemoteUpdateAction.SET_TRAVEL
                ).onFailure { emitError(it) }
                _campaignUiState.value = CampaignUiState.Idle
            }
        }
    }

    fun drawChallengeCard(): Int? {
        val drawCardId = currentChallengeDeck.value?.draw()
        viewModelScope.launch {
            campaign.value?.id?.let {
                campaignsRepository.upsertChallengeDeck(
                    it,
                    _currentChallengeDeck.value?.getDeckAsList() ?: emptyList()
                )
            }
        }
        return drawCardId
    }

    fun scoutChallengeCard(): Int? = _currentChallengeDeck.value?.scout()

    fun returnChallengeCardsInAnyOrder(topList: List<Int>, bottomList: List<Int>) {
        viewModelScope.launch {
            val campaignId = campaign.value?.id
            campaignId?.let { campaignId ->
                val deck = _currentChallengeDeck.value?.getDeckAsList() ?: emptyList()
                val exclude = (topList + bottomList).toSet()
                val middleList = deck.filter { it !in exclude }
                val newList = topList + middleList + bottomList
                campaignsRepository.upsertChallengeDeck(
                    campaignId,
                    newList
                )
                _currentChallengeDeck.value?.updateDeckWithDifferentOrder(newList)
            }
        }
    }

    fun discardScoutedCards() = _currentChallengeDeck.value?.resetScoutPosition()

    fun reshuffleChallengeDeck() {
        viewModelScope.launch {
            campaignsRepository.upsertChallengeDeck(
                campaign.value!!.id,
                _currentChallengeDeck.value?.reshuffle() ?: emptyList()
            )
        }
    }

    private val _userSettings = MutableStateFlow(UserSettings(collection = persistentListOf("core")))
    private val _packId = MutableStateFlow("core")

    private val _showAllRewards = MutableStateFlow(false)
    val showAllRewards: StateFlow<Boolean> = _showAllRewards.asStateFlow()

    fun getRole(id: String): Flow<RoleCard> = cardsRepository.getRoleCardByCodeFlow(id, false)

    fun setUserSettings(settings: UserSettings) {
        _userSettings.value = settings
    }

    fun setPackId(id: String) {
        _packId.value = id
    }

    fun setShowAllRewards(showAll: Boolean) {
        _showAllRewards.value = showAll
    }


    fun removeDeckCampaign(deckId: String) {
        viewModelScope.launch {
            val campaign = campaign.value
            campaign?.let {
                if (campaign.uploaded) _campaignUiState.value = CampaignUiState.Loading
                decksRepository.removeDeckCampaign(
                    id = deckId,
                    campaignInfo = DeckCampaignInfo(
                        campaignId = campaign.id,
                        campaignName = campaign.name,
                        campaignRewards = campaign.rewards
                    ),
                    uploaded = campaign.uploaded,
                ).onFailure { emitError(it) }
                _campaignUiState.value = CampaignUiState.Idle
            }
        }
    }

    fun addDeckCampaign(deckId: String) {
        viewModelScope.launch {
            val campaign = campaign.value
            campaign?.let {
                if (campaign.uploaded) _campaignUiState.value = CampaignUiState.Loading
                decksRepository.setDeckCampaign(
                    id = deckId,
                    campaignInfo = DeckCampaignInfo(
                        campaignId = campaign.id,
                        campaignName = campaign.name,
                        campaignRewards = campaign.rewards
                    ),
                    uploaded = campaign.uploaded,
                ).onFailure { emitError(it) }
                _campaignUiState.value = CampaignUiState.Idle
            }
        }
    }

    fun downloadFriendDeck(deckId: String) {
        viewModelScope.launch {
            val campaign = campaign.value
            campaign?.let {
                _campaignUiState.value = CampaignUiState.Loading
                decksRepository.syncDeckById(deckId.toInt())
                    .onFailure {
                        emitError(it)
                        _campaignUiState.value = CampaignUiState.Idle
                    }
                    .onSuccess {
                        _campaignUiState.value = CampaignUiState.FriendDeckDownloaded(deckId)
                    }
            }
        }
    }

    fun addFriendToCampaign(friendId: String) {
        viewModelScope.launch {
            val campaign = campaign.value
            campaign?.let {
                _campaignUiState.value = CampaignUiState.Loading
                campaignsRepository.addFriendToCampaign(
                    campaignId = campaign.id,
                    friendUserId = friendId
                ).onFailure { emitError(it) }
                _campaignUiState.value = CampaignUiState.Idle
            }
        }
    }

     fun removeFriendFromCampaign(friendId: String) {
         viewModelScope.launch {
             val campaign = campaign.value
             campaign?.let {
                 _campaignUiState.value = CampaignUiState.Loading
                 campaignsRepository.removeFriendFromCampaign(
                     campaignId = campaign.id,
                     friendUserId = friendId
                 ).onFailure { emitError(it) }
                 _campaignUiState.value = CampaignUiState.Idle
             }
         }
    }

    fun uploadCampaign() {
        viewModelScope.launch {
            val campaign = campaign.value
            campaign?.let {
                if (campaign.decks.isNotEmpty())
                    emitError(UploadingCampaignWithDecksException())
                else {
                    _campaignUiState.value = CampaignUiState.Loading
                    campaignsRepository.uploadCampaign(campaign)
                        .onFailure {
                            emitError(it)
                            _campaignUiState.value = CampaignUiState.Idle
                        }
                        .onSuccess {
                            _campaignUiState.value = CampaignUiState.CampaignUploaded(it)
                        }
                }
            }
        }
    }

    fun deleteOrLeaveCampaign(isOwner: Boolean) {
        viewModelScope.launch {
            val campaign = campaign.value
            campaign?.let {
                _campaignUiState.value = CampaignUiState.Loading
                val result = if (isOwner)
                    campaignsRepository.deleteCampaignById(campaign.id, campaign.uploaded)
                else campaignsRepository.leaveCampaign(campaign.id, campaign.userId)
                result.onFailure {
                    emitError(it)
                    _campaignUiState.value = CampaignUiState.Idle
                }.onSuccess {
                    _campaignUiState.value = CampaignUiState.Deleted(it as? String)
                }
            }
        }
    }

    fun checkIfCanUndo(): Boolean {
        val campaign = campaign.value
        // Get the last travel record (if any)
        val lastTravel = campaign?.history?.lastOrNull()
        // Compute whether we can undo an "end day"
        val canUndoEndDay = (campaign?.currentDay ?: 0) > 1 && (
                lastTravel == null ||
                        (if (lastTravel.camped) lastTravel.day + 1 else lastTravel.day) < campaign.currentDay
                )
        return lastTravel != null || canUndoEndDay
    }

    fun undoTravel() {
        viewModelScope.launch {
            val campaign = campaign.value
            campaign?.let {
                if (campaign.uploaded) _campaignUiState.value = CampaignUiState.Loading
                // Get the last travel record (if any)
                val lastTravel = campaign.history.lastOrNull()
                // Compute whether we can undo an "end day"
                val canUndoEndDay = campaign.currentDay > 1 && (lastTravel == null ||
                        (if (lastTravel.camped) lastTravel.day + 1 else lastTravel.day) < campaign.currentDay)
                val newCampaign = campaign.let { campaign ->
                    if (canUndoEndDay) campaign.copy(
                        currentDay = campaign.currentDay - 1,
                    ) else if (lastTravel != null) {
                        var previousLocation = CampaignMaps.startingLocations[campaign.cycleId]!!
                        var previousPathTerrain: String? = null
                        if (campaign.history.size >= 2) {
                            val penultimateEntry = campaign.history[campaign.history.size - 2]
                            if (penultimateEntry.location.isNotEmpty()) {
                                previousLocation = penultimateEntry.location
                            }
                            previousPathTerrain = penultimateEntry.pathTerrain
                        }
                        // Adjust previous day depending on whether the last travel had 'camped'
                        val previousDay = campaign.currentDay - if (lastTravel.camped) 1 else 0
                        campaign.copy(
                            currentDay = previousDay,
                            currentLocation = previousLocation,
                            currentPathTerrain = previousPathTerrain,
                            history = it.history.dropLast(1).toImmutableList()
                        )
                    }
                    else campaign
                }
                if (canUndoEndDay) reshuffleChallengeDeck()
                campaignsRepository.updateCampaign(
                    newCampaign,
                    RemoteUpdateAction.SET_TRAVEL
                ).onFailure { error -> emitError(error) }
                _campaignUiState.value = CampaignUiState.Idle
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val rewards: StateFlow<ImmutableList<CardListItem>> =
        combine(
            _rewardsQuery,
            _packId,
            _userSettings,
            showAllRewards
        ) { query, packId, userSettings, showAll ->
            Quadruple(query, packId, userSettings, showAll)
        }.flatMapLatest { (query, packId, userSettings, showAll) ->
            getCampaignRewardsUseCase(query, userSettings, packId, showAll)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = persistentListOf()
        )

    fun getRewardByCode(cardCode: String): Flow<FullCard> =
        cardsRepository.getCardByCodeFlow(cardCode, _userSettings.value.taboo)

    fun addCampaignReward(id: String) {
        viewModelScope.launch {
            val campaign = campaign.value
            campaign?.let {
                if (campaign.uploaded) _campaignUiState.value = CampaignUiState.Loading
                campaignsRepository.updateCampaign(
                    campaign.copy(rewards = (campaign.rewards + id).toImmutableList()),
                    RemoteUpdateAction.SET_REWARDS
                ).onFailure { emitError(it) }
                _campaignUiState.value = CampaignUiState.Idle
            }
        }
    }

    fun removeCampaignReward(id: String) {
        viewModelScope.launch {
            val campaign = campaign.value
            campaign?.let {
                if (campaign.uploaded) _campaignUiState.value = CampaignUiState.Loading
                campaignsRepository.updateCampaign(
                    campaign.copy(rewards = campaign.rewards.filterNot { it == id }.toImmutableList()),
                    RemoteUpdateAction.SET_REWARDS
                ).onFailure { emitError(it) }
                _campaignUiState.value = CampaignUiState.Idle
            }
        }
    }

    fun getRemovedSetsInfo(): ImmutableMap<String, Pair<Int?, Int>> {
        val campaign = campaign.value
        val maps = CampaignMaps.generalSetsMap() + CampaignMaps.getMapLocations(false)
        val removedSets = mutableMapOf<String, Pair<Int?, Int>>()
        campaign?.removed?.forEach { removed ->
            val fromPath = Path.fromValue(removed.setId)
            val fromMaps = maps[removed.setId]
            if (fromPath != null) removedSets[removed.setId] = fromPath.iconResId to fromPath.nameResId
            else removedSets[removed.setId] = fromMaps?.iconResId to (fromMaps?.nameResId ?: R.string.text_none)
        }
        return removedSets.toImmutableMap()
    }

    fun updateCampaignRemoved(name: String) {
        viewModelScope.launch {
            val campaign = campaign.value
            campaign?.let {
                if (campaign.uploaded) _campaignUiState.value = CampaignUiState.Loading
                campaignsRepository.updateCampaign(
                    campaign.copy(removed = campaign.removed.filterNot { it.name == name }.toImmutableList()),
                    RemoteUpdateAction.SET_REMOVED
                ).onFailure { emitError(it) }
                _campaignUiState.value = CampaignUiState.Idle
            }
        }
    }

    fun getAllRemovedSets(): Map<String, Pair<Int?, Int>> {
        val campaign = campaign.value
        campaign ?: return mapOf()
        val cycleId = campaign.cycleId
        val expansions = campaign.expansions
        val maps = CampaignMaps.generalSetsMap(cycleId) +
                CampaignMaps.getMapLocations(false, cycleId, expansions)
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

    fun addCampaignRemoved(setId: String, name: String) {
        viewModelScope.launch {
            val campaign = campaign.value
            campaign?.let {
                if (campaign.uploaded) _campaignUiState.value = CampaignUiState.Loading
                val newRemovedList = campaign.removed.toMutableList()
                newRemovedList.add(CampaignRemoved(name, setId))
                campaignsRepository.updateCampaign(
                    campaign.copy(removed = newRemovedList.toImmutableList()),
                    RemoteUpdateAction.SET_REMOVED
                ).onFailure { emitError(it) }
                _campaignUiState.value = CampaignUiState.Idle
            }
        }
    }

    fun recordCampaignEvent(name: String) {
        viewModelScope.launch {
            val campaign = campaign.value
            campaign?.let {
                if (campaign.uploaded) _campaignUiState.value = CampaignUiState.Loading
                val newEventsList = (campaign.events + CampaignEvent(name)).toImmutableList()
                campaignsRepository.updateCampaign(
                    campaign.copy(events = newEventsList),
                    RemoteUpdateAction.SET_EVENTS
                ).onFailure { emitError(it) }
                _campaignUiState.value = CampaignUiState.Idle
            }
        }
    }

    fun updateCampaignEvents(oldName: String, event: CampaignEvent) {
        viewModelScope.launch {
            val campaign = campaign.value
            campaign?.let {
                if (campaign.uploaded) _campaignUiState.value = CampaignUiState.Loading
                val newEventsList = if (event.name.isNotBlank()) campaign.events.map {
                    if (it.name == oldName) event else it
                } else campaign.events.filterNot { it.name == oldName }
                campaignsRepository.updateCampaign(
                    campaign.copy(events = newEventsList.toImmutableList()),
                    RemoteUpdateAction.SET_EVENTS
                ).onFailure { emitError(it) }
                _campaignUiState.value = CampaignUiState.Idle
            }
        }
    }

    fun addCampaignNote(day: Int, text: String) {
        viewModelScope.launch {
            val campaign = campaign.value
            campaign?.let {
                if (campaign.uploaded) _campaignUiState.value = CampaignUiState.Loading
                val newNotesList = (campaign.notes + CampaignNote(day, text)).toImmutableList()
                campaignsRepository.updateCampaign(
                    campaign.copy(notes = newNotesList),
                    RemoteUpdateAction.SET_NOTES
                ).onFailure { emitError(it) }
                _campaignUiState.value = CampaignUiState.Idle
            }
        }
    }

    fun updateCampaignNotes(oldIndex: Int, note: CampaignNote) {
        viewModelScope.launch {
            val campaign = campaign.value
            campaign?.let {
                if (campaign.uploaded) _campaignUiState.value = CampaignUiState.Loading
                val newNotesList = if (note.text.isNotBlank()) campaign.notes.mapIndexed { index, value ->
                    if (index == oldIndex) note else value
                } else campaign.notes.filterIndexed { index, _ -> index != oldIndex }
                campaignsRepository.updateCampaign(
                    campaign.copy(notes = newNotesList.toImmutableList()),
                    RemoteUpdateAction.SET_NOTES
                ).onFailure { emitError(it) }
                _campaignUiState.value = CampaignUiState.Idle
            }
        }
    }

    fun addCampaignMission(day: Int, name: String) {
        viewModelScope.launch {
            val campaign = campaign.value
            campaign?.let {
                if (campaign.uploaded) _campaignUiState.value = CampaignUiState.Loading
                val newMissionsList = (campaign.missions + CampaignMission(day, name)).toImmutableList()
                campaignsRepository.updateCampaign(
                    campaign.copy(missions = newMissionsList),
                    RemoteUpdateAction.SET_MISSION
                ).onFailure { emitError(it) }
                _campaignUiState.value = CampaignUiState.Idle
            }
        }
    }

    fun setCampaignMissions(oldName: String, mission: CampaignMission) {
        viewModelScope.launch {
            val campaign = campaign.value
            campaign?.let {
                if (campaign.uploaded) _campaignUiState.value = CampaignUiState.Loading
                val newMissionsList = campaign.missions.map {
                    if (it.name == oldName) mission else it
                }.toImmutableList()
                campaignsRepository.updateCampaign(
                    campaign.copy(missions = newMissionsList),
                    RemoteUpdateAction.SET_MISSION
                ).onFailure { emitError(it) }
                _campaignUiState.value = CampaignUiState.Idle
            }
        }
    }

    fun deleteCampaignMission(name: String) {
        viewModelScope.launch {
            val campaign = campaign.value
            campaign?.let {
                if (campaign.uploaded) _campaignUiState.value = CampaignUiState.Loading
                val newMissionsList = campaign.missions.filterNot { it.name == name }.toImmutableList()
                campaignsRepository.updateCampaign(
                    campaign.copy(missions = newMissionsList),
                    RemoteUpdateAction.SET_MISSION
                ).onFailure { emitError(it) }
                _campaignUiState.value = CampaignUiState.Idle
            }
        }
    }

    fun updateCampaignExpansions(expansions: List<String>) {
        viewModelScope.launch {
            val campaign = campaign.value
            campaign?.let {
                if (expansions.toSet() != campaign.expansions.toSet()) {
                    if (campaign.uploaded) _campaignUiState.value = CampaignUiState.Loading
                    campaignsRepository.updateCampaign(
                        campaign.copy(expansions = expansions.toImmutableList()),
                        RemoteUpdateAction.SET_EXPANSIONS
                    ).onFailure { emitError(it) }
                    _campaignUiState.value = CampaignUiState.Idle
                }
            }
        }
    }
}
