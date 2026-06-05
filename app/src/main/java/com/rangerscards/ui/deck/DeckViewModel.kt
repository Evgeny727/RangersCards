package com.rangerscards.ui.deck

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rangerscards.R
import com.rangerscards.UiErrorState
import com.rangerscards.domain.model.CardDeckListItem
import com.rangerscards.domain.model.CardWithCount
import com.rangerscards.domain.model.Deck
import com.rangerscards.domain.model.DeckMeta
import com.rangerscards.domain.model.FullCard
import com.rangerscards.domain.model.OftenUpdatableDeckValues
import com.rangerscards.domain.model.RoleCard
import com.rangerscards.domain.repository.CardsRepository
import com.rangerscards.domain.repository.DecksRepository
import com.rangerscards.domain.usecase.BuildExtraSlotsUseCase
import com.rangerscards.domain.usecase.BuildOrderedSlotsUseCase
import com.rangerscards.domain.usecase.GetRoleCardByCodeFlowUseCase
import com.rangerscards.ui.decks.CURRENT_TABOO_SET
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi

sealed interface DeckUiState {
    object Idle : DeckUiState
    object Loading : DeckUiState
    object Editing : DeckUiState
    data class DeckToOpen(val deckId: String? = null) : DeckUiState
    data class DeckUploaded(val deckId: String) : DeckUiState
}

data class ChangedCardsCategory(
    @StringRes val title: Int,
    val cards: ImmutableList<CardWithCount>
)

data class DeckErrors(
    val problems: ImmutableList<String>,
    val splash: Int?
)

@HiltViewModel
class DeckViewModel @Inject constructor(
    private val decksRepository: DecksRepository,
    private val cardsRepository: CardsRepository,
    private val getRoleCardByCodeFlowUseCase: GetRoleCardByCodeFlowUseCase,
    private val buildOrderedSlotsUseCase: BuildOrderedSlotsUseCase,
    private val buildExtraSlotsUseCase: BuildExtraSlotsUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val deckId: String = checkNotNull(savedStateHandle["deckId"])

    val deck: StateFlow<Deck?> =
        decksRepository.getDeckByIdFlow(deckId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )

    private var _deckUiState = MutableStateFlow<DeckUiState>(DeckUiState.Idle)
    val deckUiState: StateFlow<DeckUiState> = _deckUiState.asStateFlow()

    private val _events = MutableSharedFlow<UiErrorState>(extraBufferCapacity = 1)
    val events: SharedFlow<UiErrorState> = _events

    private fun emitError(throwable: Throwable) {
        _events.tryEmit(UiErrorState(throwable))
    }

    private val _updatableValues = MutableStateFlow<OftenUpdatableDeckValues?>(null)
    val updatableValues: StateFlow<OftenUpdatableDeckValues?> = _updatableValues.asStateFlow()

    init {
        viewModelScope.launch {
            deck.collect { deck ->
                deck?.let {
                    _updatableValues.value = deck.oftenUpdatableDeckValues
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val deckRole: StateFlow<RoleCard?> = deck.flatMapLatest { deck ->
        if (deck == null) flowOf(null)
        else getRoleCardByCodeFlowUseCase(deck.deckMeta.roleId, deck.tabooSetId != null)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val changedCards: StateFlow<ImmutableList<ChangedCardsCategory>> =
        deck.mapLatest { deck ->
            deck?.let {
                deck.previousDeck?.let { previousDeck ->
                    val values = deck.oftenUpdatableDeckValues
                    computeDeckChanges(
                        values.slots,
                        values.sideSlots,
                        previousDeck.slots,
                        previousDeck.sideSlots,
                        deck.tabooSetId
                    )
                }
            } ?: persistentListOf()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = persistentListOf()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val slotsCards: StateFlow<ImmutableList<CardDeckListItem>> =
        combine(_updatableValues, deck) { values, deck ->
            values?.slots?.keys?.toList().orEmpty() to deck?.tabooSetId
        }.distinctUntilChanged().flatMapLatest { (ids, taboo) ->
            cardsRepository.getDeckCardsByIdFlow(ids, taboo)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = persistentListOf()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val orderedSlotsCards: StateFlow<ImmutableMap<String, ImmutableList<CardWithCount>>> =
        combine(_updatableValues, slotsCards) { values, cards ->
            values?.slots.orEmpty() to cards
        }.map { (values, slots) ->
            buildOrderedSlotsUseCase(
                slots,
                values,
                deck.value?.deckMeta
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = persistentMapOf()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val extraSlotsCards: StateFlow<ImmutableList<CardWithCount>> =
        combine(_updatableValues, deck) { values, deck ->
            values?.extraSlots?.keys?.toList().orEmpty() to deck?.tabooSetId
        }.distinctUntilChanged().flatMapLatest { (ids, taboo) ->
            cardsRepository.getDeckCardsByIdFlow(ids, taboo)
        }.map { slots ->
            buildExtraSlotsUseCase(slots, _updatableValues.value?.extraSlots.orEmpty())
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = persistentListOf()
        )

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val deckProblems: StateFlow<DeckErrors> =
        combine(slotsCards, _updatableValues, deck) { slots, values, deck ->
            Triple(slots, values, deck?.deckMeta)
        }.debounce(1_000L).mapLatest { (slots, values, deckMeta) ->
            if (deckMeta != null) parseDeckForErrors(
                listOf(values?.awa, values?.spi, values?.fit, values?.foc),
                slots,
                values?.slots ?: emptyMap(),
                deckMeta
            ) else DeckErrors(persistentListOf(), null)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DeckErrors(persistentListOf(), null)
        )

    private suspend fun computeDeckChanges(
        slots: ImmutableMap<String, Int>,
        sideSlots: ImmutableMap<String, Int>,
        previousSlots: ImmutableMap<String, Int>,
        previousSideSlots: ImmutableMap<String, Int>,
        tabooSetId: String?
    ) : ImmutableList<ChangedCardsCategory> {
        // Prepare mutable maps to track changes.
        val addedCards = mutableMapOf<String, Int>()
        val removedCards = mutableMapOf<String, Int>()
        val addedCollectionCards = mutableMapOf<String, Int>()
        val returnedCollectionCards = mutableMapOf<String, Int>()

        // Get the union of all keys from the current and previous decks.
        val allCodes = (slots + sideSlots + previousSlots + previousSideSlots).keys

        // Process each card code.
        for (code in allCodes) {
            // Get the current and previous counts, defaulting to 0 if missing.
            val currentSlot = slots[code] ?: 0
            val currentSide = sideSlots[code] ?: 0
            val prevSlot = previousSlots[code] ?: 0
            val prevSide = previousSideSlots[code] ?: 0

            // If there is no change in both the main and side slots, skip.
            if (currentSlot == prevSlot && currentSide == prevSide) continue

            // Check if the overall count remains the same.
            if ((currentSlot + currentSide) == (prevSlot + prevSide)) {
                // Normal swaps: only the distribution between main and side has changed.
                val difference = currentSlot - prevSlot
                if (difference > 0) addedCards[code] = difference
                else removedCards[code] = difference
            } else {
                // Collection swaps: the total number of cards has changed.
                val difference = (currentSlot + currentSide) - (prevSlot + prevSide)
                if (difference > 0) addedCollectionCards[code] = difference
                else returnedCollectionCards[code] = difference
            }
        }
        val allIds = (addedCards + removedCards + addedCollectionCards + returnedCollectionCards).keys
        val allCardsMap = cardsRepository.getChangedDeckCardsById(allIds.toList(), tabooSetId)
            .associateBy { it.code }

        return persistentListOf(
            ChangedCardsCategory(
                R.string.deck_changes_added,
                addedCards.map { CardWithCount(allCardsMap[it.key]!!, it.value) }
                    .toImmutableList()
            ),
            ChangedCardsCategory(
                R.string.deck_changes_removed,
                removedCards.map { CardWithCount(allCardsMap[it.key]!!, it.value) }
                    .toImmutableList()
            ),
            ChangedCardsCategory(
                R.string.deck_changes_added_collection,
                addedCollectionCards.map { CardWithCount(allCardsMap[it.key]!!, it.value) }
                    .toImmutableList()
            ),
            ChangedCardsCategory(
                R.string.deck_changes_returned_collection,
                returnedCollectionCards.map { CardWithCount(allCardsMap[it.key]!!, it.value) }
                    .toImmutableList()
            )
        )
    }

    private fun parseDeckForErrors(
        statsList: List<Int?>,
        cards: List<CardDeckListItem>,
        slots: Map<String, Int>,
        deckMeta: DeckMeta,
    ): DeckErrors {
        val isUpgrade = deck.value?.previousDeck != null
        val problems = mutableSetOf<String>()
        // Build stats mapping
        val stats = mapOf(
            "AWA" to (statsList[0] ?: 3),
            "SPI" to (statsList[1] ?: 3),
            "FIT" to (statsList[2] ?: 3),
            "FOC" to (statsList[3] ?: 3),
        )

        if (stats.values.sum() != 8 || stats.values.none { it == 1 } || stats.values.count { it >= 3 } > 1)
            problems.add("invalid_aspects")

        var splashCount = 0
        @StringRes
        var splashResId: Int? = null
        val deckSize = cards.associateWith { card -> slots[card.code] ?: 0 }
            .entries.sumOf { if (it.key.setId != "malady") it.value else 0 }

        cards.forEach { card ->
            val cardCount = slots[card.code] ?: 0
            if (cardCount > 2) {
                if (card.setId != "malady") {
                    problems.add("too_many_duplicates")
                }
            } else if (!isUpgrade && cardCount != 2) {
                problems.add("need_two_cards")
            }
            if (card.aspect != null && card.level != null && ((stats[card.aspect!!.id] ?: 0) < card.level!!))
                problems.add("invalid_aspect_levels")
        }
        if (isUpgrade) {
            if (deckSize < 30) problems.add("too_few_cards")
            else if (deckSize > 30) problems.add("too_many_cards")
        } else {
            // Additional rules for starting decks:
            var backgroundNonExpert = 0
            var backgroundCount = 0
            val background = deckMeta.background
            var specialtyNonExpert = 0
            var specialtyCount = 0
            val specialty = deckMeta.specialty
            val personalityCount = mutableMapOf(
                "AWA" to 0,
                "FIT" to 0,
                "FOC" to 0,
                "SPI" to 0
            )
            cards.forEach { card ->
                val cardCount = slots[card.code] ?: 0
                val isExpert = card.realTraits?.contains("Expert") == true
                when(card.setId) {
                    "personality" -> {
                        when (card.aspect?.id) {
                            "AWA" -> {
                                personalityCount["AWA"] = personalityCount.getValue("AWA") + 2
                                if (personalityCount.getValue("AWA") > 2) problems.add("too_many_awa_personality")
                            }
                            "FOC" -> {
                                personalityCount["FOC"] = personalityCount.getValue("FOC") + 2
                                if (personalityCount.getValue("FOC") > 2) problems.add("too_many_foc_personality")
                            }
                            "FIT" -> {
                                personalityCount["FIT"] = personalityCount.getValue("FIT") + 2
                                if (personalityCount.getValue("FIT") > 2) problems.add("too_many_fit_personality")
                            }
                            "SPI" -> {
                                personalityCount["SPI"] = personalityCount.getValue("SPI") + 2
                                if (personalityCount.getValue("SPI") > 2) problems.add("too_many_spi_personality")
                            }
                        }
                    }
                    else -> when (card.setTypeId) {
                        "background" -> if (card.setId == background) {
                            backgroundCount += cardCount

                            if (card.realTraits == null || !isExpert)
                                backgroundNonExpert += cardCount

                            if (backgroundCount > 10) {
                                if (backgroundCount > 12 || splashCount >= 2)
                                    problems.add("too_many_background")
                                else if (backgroundNonExpert < 2)
                                    problems.add("invalid_outside_interest")
                                else {
                                    splashResId = R.string.background_as_outside_interest
                                    splashCount += cardCount
                                }
                            }
                        } else {
                            if (card.realTraits != null && isExpert)
                                problems.add("invalid_outside_interest")
                            else {
                                splashCount += cardCount
                                if (splashCount > 2) {
                                    problems.add("too_many_outside_interest")
                                }
                            }
                        }
                        "specialty" -> if (card.setId == specialty) {
                            specialtyCount += cardCount

                            if (card.realTraits == null || !isExpert)
                                specialtyNonExpert += cardCount

                            if (specialtyCount > 10) {
                                if (specialtyCount > 12 || splashCount >= 2) {
                                    problems.add("too_many_specialty")
                                } else if (specialtyNonExpert < 2) {
                                    problems.add("invalid_outside_interest")
                                } else {
                                    splashResId = R.string.specialty_as_outside_interest
                                    splashCount += cardCount
                                }
                            }
                        } else {
                            if (card.realTraits != null && isExpert) {
                                problems.add("invalid_outside_interest")
                            } else {
                                splashCount += cardCount
                                if (splashCount > 2) {
                                    problems.add("too_many_outside_interest")
                                }
                            }
                        }
                    }
                }
            }
            // Validate personality counts: each aspect must equal exactly 2.
            if (personalityCount["AWA"] != 2 ||
                personalityCount["FIT"] != 2 ||
                personalityCount["FOC"] != 2 ||
                personalityCount["SPI"] != 2
            ) { problems.add("personality") }
            if (backgroundCount < 10) problems.add("background")
            if (specialtyCount < 10) problems.add("specialty")
            if (splashCount < 2) problems.add("outside_interest")
        }
        return DeckErrors(
            problems.toImmutableList(),
            if (splashCount == 2) splashResId else null
        )
    }

    fun enterEditMode() {
        _deckUiState.value = DeckUiState.Editing
    }

    fun addCard(id: String) {
        val previousDeck = deck.value?.previousDeck
        _updatableValues.update { values ->
            values?.let {
                if (previousDeck == null) it.copy(slots = it.slots.put(id, 2))
                else it.sideSlots[id]?.let { sideSlot ->
                    it.copy(
                        slots = it.slots.put(id, (it.slots[id] ?: 0) + 1),
                        sideSlots = if (sideSlot > 1)
                            it.sideSlots.put(id, sideSlot - 1)
                        else it.sideSlots.remove(id)
                    )
                } ?: it.copy(slots = it.slots.put(id, (it.slots[id] ?: 0) + 1))
            }
        }
    }

    fun removeCard(id: String, setId: String?) {
        val previousDeck = deck.value?.previousDeck
        _updatableValues.update { values ->
            values?.let {
                if (previousDeck == null) it.copy(slots = it.slots.remove(id))
                else if ((it.slots[id] ?: 0) > 1) it.copy(
                    slots = it.slots.put(id, it.slots[id]!! - 1),
                    sideSlots = if (setId == "reward" || setId == "malady") it.sideSlots
                        else it.sideSlots.put(id, (it.sideSlots[id] ?: 0) + 1)
                ) else it.copy(
                    slots = it.slots.remove(id),
                    sideSlots = if (setId == "reward" || setId == "malady") it.sideSlots
                    else it.sideSlots.put(id, (it.sideSlots[id] ?: 0) + 1)
                )
            }
        }
    }

    fun addExtraCard(id: String) {
        _updatableValues.update { it?.copy(extraSlots = it.extraSlots.put(id, 1)) }
    }

    fun removeExtraCard(id: String) {
        _updatableValues.update { it?.copy(extraSlots = it.extraSlots.remove(id)) }
    }

    fun checkChanges(): Boolean = _deckUiState.value is DeckUiState.Editing &&
            deck.value?.oftenUpdatableDeckValues != _updatableValues.value

    fun saveChanges(needSaving: Boolean = false): Job? {
        if (needSaving || checkChanges()) {
            val deck = deck.value
            deck?.let { deck ->
                val values = updatableValues.value
                values?.let { values ->
                    return viewModelScope.launch {
                        _deckUiState.value = DeckUiState.Loading
                        decksRepository.saveDeck(deck.copy(
                            deckMeta = deck.deckMeta.copy(
                                problems = deckProblems.value.problems.ifEmpty { null }
                            ),
                            oftenUpdatableDeckValues = values
                        )).onFailure { emitError(it) }
                        _deckUiState.value = DeckUiState.Idle
                    }
                }
            }
        } else _deckUiState.value = DeckUiState.Idle
        return null
    }

    fun discardChanges() {
        _deckUiState.value = DeckUiState.Idle
        _updatableValues.value = deck.value?.oftenUpdatableDeckValues
    }

    fun changeStat(index: Int, newValue: Int) {
        _updatableValues.update {
            it?.let {
                when(index) {
                    0 -> it.copy(awa = newValue)
                    1 -> it.copy(spi = newValue)
                    2 -> it.copy(fit = newValue)
                    else -> it.copy(foc = newValue)
                }
            }
        }
    }

    fun camp() {
        val deck = deck.value
        deck?.let { deck ->
            val values = updatableValues.value
            values?.let { values ->
                viewModelScope.launch {
                    _deckUiState.value = DeckUiState.Loading
                    decksRepository.upgradeDeck(
                        id = deck.id,
                        uploaded = deck.uploaded
                    ).onFailure {
                        emitError(it)
                        _deckUiState.value = DeckUiState.Idle
                    }.onSuccess {
                        _deckUiState.value = DeckUiState.DeckToOpen(it)
                    }
                }
            }
        }
    }


    @OptIn(ExperimentalUuidApi::class)
    fun cloneDeck(isUpload: Boolean, newName: String, postfix: String) {
        val deck = deck.value
        deck?.let { deck ->
            val values = updatableValues.value
            values?.let { values ->
                viewModelScope.launch {
                    _deckUiState.value = DeckUiState.Loading
                    decksRepository.createDeck(
                        uploaded = isUpload,
                        name = if (newName.trim() == deck.name) "${deck.name} $postfix" else newName,
                        slots = values.slots,
                        extraSlots = values.extraSlots,
                        meta = deck.deckMeta,
                        tabooSetId = deck.tabooSetId,
                        awa = values.awa,
                        spi = values.spi,
                        fit = values.fit,
                        foc = values.foc
                    ).onFailure {
                        emitError(it)
                        _deckUiState.value = DeckUiState.Idle
                    }.onSuccess {
                        _deckUiState.value = DeckUiState.DeckUploaded(it)
                    }
                }
            }
        }
    }

    fun updateDeckName(newName: String) {
        val deck = deck.value
        deck?.let { deck ->
            if (deck.name == newName) return@let
            val values = updatableValues.value
            values?.let { values ->
                viewModelScope.launch {
                    _deckUiState.value = DeckUiState.Loading
                    decksRepository.saveDeck(
                        deck.copy(
                            name = newName,
                            deckMeta = deck.deckMeta.copy(problems = deckProblems.value.problems)
                        )
                    ).onFailure { emitError(it)}
                    _deckUiState.value = DeckUiState.Idle
                }
            }
        }
    }

    fun uploadDeck() {
        val deck = deck.value
        deck?.let { deck ->
            val values = updatableValues.value
            values?.let { values ->
                viewModelScope.launch {
                    _deckUiState.value = DeckUiState.Loading
                    decksRepository.createDeck(
                        uploaded = true,
                        name = deck.name,
                        slots = values.slots,
                        extraSlots = values.extraSlots,
                        meta = deck.deckMeta,
                        tabooSetId = deck.tabooSetId,
                        awa = values.awa,
                        spi = values.spi,
                        fit = values.fit,
                        foc = values.foc
                    ).onFailure {
                        emitError(it)
                        _deckUiState.value = DeckUiState.Idle
                    }.onSuccess {
                        _deckUiState.value = DeckUiState.DeckUploaded(it)
                    }
                }
            }
        }
    }

    fun deleteDeck() {
        val deck = deck.value
        deck?.let { deck ->
            val values = updatableValues.value
            values?.let { values ->
                viewModelScope.launch {
                    _deckUiState.value = DeckUiState.Loading
                    decksRepository.deleteDeckById(
                        id = deck.id,
                        uploaded = deck.uploaded,
                    ).onFailure {
                        emitError(it)
                        _deckUiState.value = DeckUiState.Idle
                    }.onSuccess {
                        _deckUiState.value = DeckUiState.DeckToOpen(it)
                    }
                }
            }
        }
    }

    fun deleteAllVersionsOfDeck() {
        val deck = deck.value
        deck?.let { deck ->
            val values = updatableValues.value
            values?.let { values ->
                viewModelScope.launch {
                    _deckUiState.value = DeckUiState.Loading
                    decksRepository.deleteAllDeckVersionsById(
                        id = deck.id,
                        uploaded = deck.uploaded,
                    ).onFailure {
                        emitError(it)
                        _deckUiState.value = DeckUiState.Idle
                    }.onSuccess {
                        _deckUiState.value = DeckUiState.DeckToOpen(null)
                    }
                }
            }
        }
    }

    fun setDeckTaboo(taboo: Boolean) {
        val deck = deck.value
        deck?.let { deck ->
            val values = updatableValues.value
            values?.let { values ->
                viewModelScope.launch {
                    _deckUiState.value = DeckUiState.Loading
                    decksRepository.saveDeckTabooSet(
                        id = deck.id,
                        tabooId = if (taboo) CURRENT_TABOO_SET else null,
                        uploaded = deck.uploaded,
                    ).onFailure { emitError(it) }
                    _deckUiState.value = DeckUiState.Idle
                }
            }
        }
    }

    fun getCardById(cardCode: String, taboo: Boolean): Flow<FullCard?> =
        cardsRepository.getCardByCodeFlow(cardCode, taboo)

    fun changeRole(background: String, specialty: String, role: String) {
        val deck = deck.value
        deck?.let { deck ->
            val values = updatableValues.value
            values?.let { values ->
                viewModelScope.launch {
                    _deckUiState.value = DeckUiState.Loading
                    decksRepository.saveDeck(
                        deck.copy(deckMeta = deck.deckMeta.copy(
                            roleId = role,
                            background = background,
                            specialty = specialty,
                            problems = deckProblems.value.problems
                        ))
                    ).onFailure { emitError(it) }
                    _deckUiState.value = DeckUiState.Idle
                }
            }
        }
    }
}