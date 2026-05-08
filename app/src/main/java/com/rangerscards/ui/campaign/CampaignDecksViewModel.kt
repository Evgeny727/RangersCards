package com.rangerscards.ui.campaign

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.rangerscards.domain.model.DeckListItem
import com.rangerscards.domain.usecase.SearchDecksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class CampaignDecksViewModel @Inject constructor(
    private val searchDecksUseCase: SearchDecksUseCase
) : ViewModel() {

    // Holds the current search term entered by the user.
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _uploaded = MutableStateFlow(false)

    private val _userId = MutableStateFlow("")

    // Exposes the paginated search results as PagingData.
    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults: Flow<PagingData<DeckListItem>> =
        combine(_searchQuery, _uploaded, _userId) { query, uploaded, userId ->
            Triple(query.trim(), uploaded, userId)
        }.flatMapLatest { (query, uploaded, userId) ->
            searchDecksUseCase(query, userId, uploaded, true)
        }.cachedIn(viewModelScope)

    fun setUploaded(uploaded: Boolean) {
        _uploaded.value = uploaded
    }

    fun setUserId(userId: String) {
        _userId.value = userId
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun clearSearchQuery() {
        _searchQuery.value = ""
    }
}