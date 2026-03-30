package com.rangerscards.ui.cards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.rangerscards.objects.CardFilterOptions
import com.rangerscards.data.local.card.CardListItemProjection
import com.rangerscards.data.local.card.FullCardProjection
import com.rangerscards.domain.repository.CardsRepository
import com.rangerscards.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

data class Quintuple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)

class CardsViewModel(
    private val cardsRepository: CardsRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    // Holds the current state of whether to include English search results.
    private val _includeEnglish: StateFlow<Boolean> =
        userPreferencesRepository.isIncludeEnglishSearchResults.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    // Holds the current spoiler state.
    private val _spoiler = MutableStateFlow(false)
    val spoiler: StateFlow<Boolean> = _spoiler.asStateFlow()

    private val _taboo = MutableStateFlow(false)
    private val _packIds = MutableStateFlow(listOf("core"))

    private val _filterOptions = MutableStateFlow(CardFilterOptions())
    val filterOptions: StateFlow<CardFilterOptions> = _filterOptions.asStateFlow()

    init {
        // launch a single, one-shot read
        viewModelScope.launch {
            val sortOrder = userPreferencesRepository.sortOrder.first()
            if (sortOrder.isNotEmpty()) _filterOptions.update { it.copy(sortOrder = sortOrder) }
        }
    }

    // Exposes the paginated search results as PagingData.
    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults: Flow<PagingData<CardListItemProjection>> =
        combine(_filterOptions, _includeEnglish, _spoiler, _taboo, _packIds) { filterOptions, include, spoiler, taboo, packIds ->
            Quintuple(filterOptions, include, spoiler, taboo, packIds)
        }.flatMapLatest { (filterOptions, include, spoiler, taboo, packIds) ->
            // When the search query or include flag changes, perform a new search.
            if (filterOptions.searchQuery.isEmpty()) {
                cardsRepository.getAllCards(spoiler, taboo, packIds, filterOptions).catch { throwable ->
                    // Log the error.
                    throwable.printStackTrace()
                    // Return an empty PagingData on error so that the flow continues.
                    emit(PagingData.empty())
                }
            } else {
                cardsRepository.searchCards(
                    filterOptions = filterOptions,
                    includeEnglish = include,
                    spoiler = spoiler,
                    language = Locale.getDefault().language.substring(0..1),
                    taboo = taboo,
                    packIds = packIds
                ).catch { throwable ->
                    // Log the error.
                    throwable.printStackTrace()
                    // Return an empty PagingData on error so that the flow continues.
                    emit(PagingData.empty())
                }
            }
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
        _filterOptions.update { it.copy(
            types = newFilterOptions.types,
            traits = newFilterOptions.traits,
            sets = newFilterOptions.sets,
            costRange = newFilterOptions.costRange,
            approaches = newFilterOptions.approaches,
            packs = newFilterOptions.packs,
            aspectRequirements = newFilterOptions.aspectRequirements
        ) }
    }

    fun clearFilterOptions() {
        _filterOptions.update { CardFilterOptions(
            searchQuery = it.searchQuery,
            sortOrder = it.sortOrder
        ) }
    }

    fun applyNewSortOptions(newSortOptions: List<String>) {
        _filterOptions.update { it.copy(sortOrder = newSortOptions.ifEmpty {
                listOf("set_type_id", "set_id", "set_position")
            })
        }
        viewModelScope.launch {
            userPreferencesRepository.saveSortOrderPreference(newSortOptions)
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

        viewModelScope.launch {
            userPreferencesRepository.saveSortOrderPreference(emptyList())
        }
    }

    /**
     * Called when the user switches spoiler.
     */
    fun onSpoilerChanged() {
        _spoiler.update { !it }
    }

    fun setTabooId(taboo: Boolean?) {
        _taboo.update { taboo ?: false }
    }

    fun setPackIds(packIds: List<String>) {
        _packIds.update { listOf("core") + packIds }
    }

    fun getCardById(cardCode: String): Flow<FullCardProjection?> =
        cardsRepository.getCardById(cardCode, _taboo.value)
}