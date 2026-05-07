package com.rangerscards.ui.decks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.rangerscards.UiErrorState
import com.rangerscards.domain.model.DeckListItem
import com.rangerscards.domain.model.RoleCard
import com.rangerscards.domain.repository.AuthRepository
import com.rangerscards.domain.repository.DecksRepository
import com.rangerscards.domain.usecase.GetRoleCardByCodeFlowUseCase
import com.rangerscards.domain.usecase.SearchDecksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

const val CURRENT_TABOO_SET = "set_01"

sealed interface DecksUiState {
    object Idle : DecksUiState
    object Loading : DecksUiState
}

@HiltViewModel
class DecksViewModel @Inject constructor(
    authRepository: AuthRepository,
    private val decksRepository: DecksRepository,
    private val getRoleCardByCodeFlowUseCase: GetRoleCardByCodeFlowUseCase,
    private val searchDecksUseCase: SearchDecksUseCase,
) : ViewModel() {

    private val _decksUiState = MutableStateFlow<DecksUiState>(DecksUiState.Idle)
    val decksUiState: StateFlow<DecksUiState> = _decksUiState.asStateFlow()

    // Holds the current search term entered by the user.
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _events = MutableSharedFlow<UiErrorState>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val events: SharedFlow<UiErrorState> = _events

    private fun emitError(throwable: Throwable) {
        _events.tryEmit(UiErrorState(throwable))
    }

    fun getAllNetworkDecks(userId: String?) {
        viewModelScope.launch {
            _decksUiState.value = DecksUiState.Loading
            userId?.let {
                decksRepository.syncDecks(userId)
                    .onFailure { emitError(it) }
            }
            _decksUiState.value = DecksUiState.Idle
        }
    }

    // Exposes the paginated search results as PagingData.
    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults: Flow<PagingData<DeckListItem>> =
        combine(_searchQuery, authRepository.currentUserId) { query, userIdFlow ->
            Pair(query, userIdFlow)
        }.flatMapLatest { (query, userIdFlow) ->
            searchDecksUseCase(query, userIdFlow ?: "")
        }.cachedIn(viewModelScope)

    fun getRoleCard(code: String, taboo: Boolean): Flow<RoleCard> =
        getRoleCardByCodeFlowUseCase(code, taboo)

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
}