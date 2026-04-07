package com.rangerscards.ui.deck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.rangerscards.objects.CardFilterOptions
import com.rangerscards.data.repository.UserPreferencesRepository
import com.rangerscards.data.local.card.CardDeckListItemProjection
import com.rangerscards.domain.model.Deck
import com.rangerscards.ui.cards.Quintuple
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.util.Locale

class DeckCardsViewModel(
    private val deckRepository: DeckRepository,
    userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val _deckInfo= MutableStateFlow<DeckInfo?>(null)

    private val _showAllSpoilers = MutableStateFlow(false)
    val showAllSpoilers = _showAllSpoilers.asStateFlow()

    // Holds the current state of whether to include English search results.
    private val _includeEnglish: StateFlow<Boolean> =
        userPreferencesRepository.isIncludeEnglishSearchResults.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    // Holds the current type index.
    private val _typeIndex = MutableStateFlow(0)
    val typeIndex: StateFlow<Int> = _typeIndex.asStateFlow()

    private val _packIds = MutableStateFlow(listOf("core"))

    private val _filterOptions = MutableStateFlow(CardFilterOptions())
    val filterOptions: StateFlow<CardFilterOptions> = _filterOptions.asStateFlow()

    // Exposes the paginated search results as PagingData.
    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults: Flow<PagingData<CardDeckListItemProjection>> =
        combine(_filterOptions, _deckInfo, _typeIndex, _showAllSpoilers, _includeEnglish) { filterOptions, deckInfo, typeIndex, showAllSpoilers, includeEnglish ->
            Quintuple(filterOptions, deckInfo, typeIndex, showAllSpoilers, includeEnglish)
        }.flatMapLatest { (filterOptions, deckInfo, typeIndex, showAllSpoilers, includeEnglish) ->
            // When the search query or include flag changes, perform a new search.
            if (deckInfo != null) {
                if (filterOptions.searchQuery.isEmpty()) {
                    deckRepository.getAllCards(deckInfo, typeIndex, showAllSpoilers, _packIds.value, filterOptions)
                        .catch { throwable ->
                            // Log the error.
                            throwable.printStackTrace()
                            // Return an empty PagingData on error so that the flow continues.
                            emit(PagingData.empty())
                        }
                } else {
                    deckRepository.searchCards(
                        filterOptions = filterOptions,
                        deckInfo = deckInfo,
                        includeEnglish = includeEnglish,
                        typeIndex = typeIndex,
                        showAllSpoilers = showAllSpoilers,
                        language = Locale.getDefault().language.substring(0..1),
                        packIds = _packIds.value
                    ).catch { throwable ->
                        // Log the error.
                        throwable.printStackTrace()
                        // Return an empty PagingData on error so that the flow continues.
                        emit(PagingData.empty())
                    }
                }
            } else flow { emit(PagingData.empty()) }
        }.cachedIn(viewModelScope)

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
        _filterOptions.update { it.copy(sortOrder = newSortOptions.ifEmpty {
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
                isUpgrade = deck.previousId != null,
                background = deck.background,
                specialty = deck.specialty,
                rewards = deck.campaignRewards ?: emptyList(),
                extraSlots = extraSlots,
                taboo = deck.tabooSetId,
            )
        }
    }

    fun updateShowAllSpoilers(newValue: Boolean) {
        _showAllSpoilers.update { newValue }
    }

    fun onTypeIndexChanged(newIndex: Int) {
        if (newIndex != -1) _typeIndex.update {
            newIndex
        }
    }

    fun setPackIds(packIds: List<String>) {
        _packIds.update { listOf("core") + packIds }
    }
}