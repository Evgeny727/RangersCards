package com.rangerscards.ui.deck

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.rangerscards.domain.model.CardDeckListItem
import com.rangerscards.domain.model.CardFilterOptions
import com.rangerscards.domain.model.Deck
import com.rangerscards.domain.model.DeckInfo
import com.rangerscards.domain.repository.UserPreferencesRepository
import com.rangerscards.domain.usecase.SearchDeckCardsUseCase
import com.rangerscards.ui.cards.Quintuple
import com.rangerscards.ui.deck.components.DeckCardListUiModel
import com.rangerscards.ui.deck.components.withCategoryHeaders
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class DeckCardsViewModel @Inject constructor(
    private val searchDeckCardsUseCase: SearchDeckCardsUseCase,
    userPreferencesRepository: UserPreferencesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _deckInfo= MutableStateFlow<DeckInfo?>(null)

    private val _showAllSpoilers = MutableStateFlow(false)
    val showAllSpoilers = _showAllSpoilers.asStateFlow()

    // Holds the current state of whether to include English search results.
    private val _includeEnglish: StateFlow<Boolean> =
        userPreferencesRepository.isIncludeEnglishSearchResults

    // Holds the current type index.
    private val _typeIndex: MutableStateFlow<Int> = MutableStateFlow(checkNotNull(savedStateHandle["typeIndexArgument"]))
    val typeIndex: StateFlow<Int> = _typeIndex.asStateFlow()

    private val _packIds = MutableStateFlow(listOf("core"))

    private val _filterOptions = MutableStateFlow(CardFilterOptions())
    val filterOptions: StateFlow<CardFilterOptions> = _filterOptions.asStateFlow()

    // Exposes the paginated search results as PagingData.
    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults: Flow<PagingData<CardDeckListItem>> =
        combine(
            _filterOptions,
            _deckInfo,
            _typeIndex,
            _showAllSpoilers,
            _includeEnglish
        ) { filterOptions, deckInfo, typeIndex, showAllSpoilers, includeEnglish ->
            Quintuple(filterOptions, deckInfo, typeIndex, showAllSpoilers, includeEnglish)
        }.flatMapLatest { (filterOptions, deckInfo, typeIndex, showAllSpoilers, includeEnglish) ->
            if (deckInfo != null)
                searchDeckCardsUseCase(
                    filterOptions,
                    deckInfo,
                    typeIndex,
                    showAllSpoilers,
                    _packIds.value,
                    includeEnglish
                )
            else flowOf(PagingData.empty())
        }.cachedIn(viewModelScope)

    val searchResultsWithHeaders: Flow<PagingData<DeckCardListUiModel>> =
        searchResults.withCategoryHeaders(_filterOptions.value.sortOrder)

    /**
     * Called when the user enters a new search term.
     */
    fun onSearchQueryChanged(newQuery: String) {
        _filterOptions.update {
            it.copy(searchQuery = newQuery)
        }
    }

    fun clearSearchQuery() {
        _filterOptions.update { it.copy(searchQuery = "") }
    }

    fun applyNewFilterOptions(newFilterOptions: CardFilterOptions) {
        _filterOptions.update { newFilterOptions.copy(searchQuery = it.searchQuery) }
    }

    fun clearFilterOptions() {
        _filterOptions.update { CardFilterOptions(searchQuery = it.searchQuery) }
    }

    fun applyNewSortOptions(newSortOptions: List<String>) {
        _filterOptions.update {
            it.copy(sortOrder = newSortOptions.ifEmpty {
                listOf("set_type_id", "set_id", "set_position")
            })
        }
    }

    fun clearSortOptions() {
        _filterOptions.update { CardFilterOptions(
            searchQuery = it.searchQuery,
            types = it.types,
            traits = it.traits,
            sets = it.sets,
            costRange = it.costRange,
            approaches = it.approaches,
            packs = it.packs,
            aspectRequirements = it.aspectRequirements
        ) }
    }

    fun updateDeckInfo(deck: Deck, extraSlots: List<String>) {
        _deckInfo.update {
            DeckInfo(
                isUpgrade = deck.previousDeck != null,
                background = deck.deckMeta.background,
                specialty = deck.deckMeta.specialty,
                rewards = deck.campaignInfo?.campaignRewards ?: emptyList(),
                extraSlots = extraSlots,
                taboo = deck.tabooSetId,
            )
        }
    }

    fun updateShowAllSpoilers(newValue: Boolean) {
        _showAllSpoilers.value = newValue
    }

    fun onTypeIndexChanged(newIndex: Int) {
        _typeIndex.value = newIndex
    }

    fun setPackIds(packIds: List<String>) {
        _packIds.value = listOf("core") + packIds
    }
}