package com.rangerscards.ui.campaigns

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.rangerscards.UiErrorState
import com.rangerscards.domain.model.CampaignListItem
import com.rangerscards.domain.repository.AuthRepository
import com.rangerscards.domain.repository.CampaignsRepository
import com.rangerscards.domain.usecase.GetRolesImagesByIdFlowUseCase
import com.rangerscards.domain.usecase.SearchCampaignsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
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

sealed interface CampaignsUiState {
    object Idle : CampaignsUiState
    object Loading : CampaignsUiState
}

@HiltViewModel
class CampaignsViewModel @Inject constructor(
    authRepository: AuthRepository,
    private val campaignsRepository: CampaignsRepository,
    private val searchCampaignsUseCase: SearchCampaignsUseCase,
    private val getRolesImagesByIdFlowUseCase: GetRolesImagesByIdFlowUseCase
) : ViewModel() {

    private val _campaignsUiState = MutableStateFlow<CampaignsUiState>(CampaignsUiState.Idle)
    val campaignsUiState: StateFlow<CampaignsUiState> = _campaignsUiState.asStateFlow()

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

    fun getAllNetworkCampaigns(userId: String?) {
        viewModelScope.launch {
            _campaignsUiState.value = CampaignsUiState.Loading
            userId?.let {
                campaignsRepository.syncCampaigns(userId)
                    .onFailure { emitError(it) }
            }
            _campaignsUiState.value = CampaignsUiState.Idle
        }
    }

    // Exposes the paginated search results as PagingData.
    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults: Flow<PagingData<CampaignListItem>> =
        combine(_searchQuery, authRepository.currentUserId) { query, userIdFlow ->
            Pair(query, userIdFlow)
        }.flatMapLatest { (query, userIdFlow) ->
            searchCampaignsUseCase(query, userIdFlow ?: "")
        }.cachedIn(viewModelScope)

    fun getRolesImages(ids: List<String>): Flow<ImmutableList<String>> =
        getRolesImagesByIdFlowUseCase(ids)

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