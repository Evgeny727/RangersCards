package com.rangerscards.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rangerscards.UiErrorState
import com.rangerscards.domain.model.User
import com.rangerscards.domain.model.UserInfo
import com.rangerscards.domain.repository.FriendAction
import com.rangerscards.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface FriendsUiState {
    object Idle : FriendsUiState
    object Loading : FriendsUiState
}

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _friendsUiState = MutableStateFlow<FriendsUiState>(FriendsUiState.Idle)
    val friendsUiState: StateFlow<FriendsUiState> = _friendsUiState.asStateFlow()

    private val _events = MutableSharedFlow<UiErrorState>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val events: SharedFlow<UiErrorState> = _events

    fun emitError(throwable: Throwable) {
        _events.tryEmit(UiErrorState(throwable))
    }

    // Holds the current search query entered by the user.
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchResults: MutableStateFlow<ImmutableList<UserInfo>> = MutableStateFlow(persistentListOf())
    val searchResults = _searchResults.asStateFlow()

    /**
     * Called when the user enters a new search term.
     */
    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun clearSearchQuery() {
        _searchQuery.value = ""
    }

    fun getUsersByHandle(handle: String, user: User? = null) {
        viewModelScope.launch {
            if (handle == "") _searchResults.value = persistentListOf()
            else {
                _friendsUiState.value = FriendsUiState.Loading
                settingsRepository.searchUsersByHandle(handle)
                    .onSuccess {
                        if (user != null) filterUsers(user, it)
                        else _searchResults.value = it.toImmutableList()
                    }
                    .onFailure {
                        emitError(it)
                        _searchResults.value = persistentListOf()
                    }
                _friendsUiState.value = FriendsUiState.Idle
            }
        }
    }

    private fun filterUsers(user: User, list: List<UserInfo>)  {
        val filtered = list.filterNot { item ->
            item in user.friends || item in user.sentRequests
                    || item in user.receivedRequests || item.id == user.userInfo?.id
        }.toImmutableList()
        _searchResults.value = filtered
    }

    private fun removeUser(userId: String): ImmutableList<UserInfo> {
        return _searchResults.value.filterNot { it.id == userId }.toImmutableList()
    }

    fun sendFriendRequest(toUserId: String) {
        viewModelScope.launch {
            _friendsUiState.value = FriendsUiState.Loading
            settingsRepository.friendRequestAction(FriendAction.SENT, toUserId)
                .onSuccess { _searchResults.value = removeUser(toUserId) }
                .onFailure { emitError(it) }
            _friendsUiState.value = FriendsUiState.Idle
        }
    }

    fun acceptFriendRequest(toUserId: String) {
        viewModelScope.launch {
            _friendsUiState.value = FriendsUiState.Loading
            settingsRepository.friendRequestAction(FriendAction.ACCEPT, toUserId)
                .onFailure { emitError(it) }
            _friendsUiState.value = FriendsUiState.Idle
        }
    }

    fun rejectFriendRequest(toUserId: String) {
        viewModelScope.launch {
            _friendsUiState.value = FriendsUiState.Loading
            settingsRepository.friendRequestAction(FriendAction.REVOKE, toUserId)
                .onFailure { emitError(it) }
            _friendsUiState.value = FriendsUiState.Idle
        }
    }

}