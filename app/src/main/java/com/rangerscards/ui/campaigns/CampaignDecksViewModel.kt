package com.rangerscards.ui.campaigns

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import com.rangerscards.data.local.deck.DeckListItemProjection
import com.rangerscards.domain.repository.CampaignsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update

class CampaignDecksViewModel(
    private val campaignRepository: CampaignsRepository
) : ViewModel() {

    // Holds the current search term entered by the user.
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _uploaded = MutableStateFlow(false)

    // Exposes the paginated search results as PagingData.
    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults: Flow<PagingData<DeckListItemProjection>> = combine(_searchQuery, _uploaded) { query, uploaded ->
        query.trim() to uploaded
    }.flatMapLatest { (query, uploaded) ->
        // When the search query or include flag changes, perform a new search.
        val userId = Firebase.auth.currentUser?.uid ?: ""
        if (query.isEmpty()) {
            campaignRepository.getAllDecks(userId, uploaded).catch { throwable ->
                // Log the error.
                throwable.printStackTrace()
                // Return an empty PagingData on error so that the flow continues.
                emit(PagingData.empty())
            }
        } else {
            campaignRepository.searchDecks(query, userId, uploaded).catch { throwable ->
                // Log the error.
                throwable.printStackTrace()
                // Return an empty PagingData on error so that the flow continues.
                emit(PagingData.empty())
            }
        }
    }.cachedIn(viewModelScope)

    fun setUploaded(uploaded: Boolean) {
        _uploaded.update { uploaded }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.update {
            newQuery
        }
    }

    fun clearSearchQuery() {
        _searchQuery.update { "" }
    }
}