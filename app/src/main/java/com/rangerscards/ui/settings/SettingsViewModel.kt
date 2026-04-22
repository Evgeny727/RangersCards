package com.rangerscards.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rangerscards.UiErrorState
import com.rangerscards.domain.model.UserInfo
import com.rangerscards.domain.repository.AuthRepository
import com.rangerscards.domain.repository.FriendAction
import com.rangerscards.domain.repository.SettingsRepository
import com.rangerscards.domain.repository.UserPreferencesRepository
import com.rangerscards.domain.usecase.ClearAllLocalDecksAndCampaignsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel to maintain user's settings.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private  val settingsRepository: SettingsRepository,
    private val clearAllLocalDecksAndCampaignsUseCase: dagger.Lazy<ClearAllLocalDecksAndCampaignsUseCase>
) : ViewModel() {

    private val _events = MutableSharedFlow<UiErrorState>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val events: SharedFlow<UiErrorState> = _events

    fun emitError(throwable: Throwable) {
        _events.tryEmit(UiErrorState(throwable))
    }

    val isIncludeEnglishSearchResultsState: StateFlow<Boolean> =
        userPreferencesRepository.isIncludeEnglishSearchResults.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    // Holds the current search query entered by the user.
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow(emptyList<UserInfo>())
    val searchResults = _searchResults.asStateFlow()

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            authRepository.signIn(email, password).onFailure { emitError(it) }
        }
    }

    fun createAccount(email: String, password: String) {
        viewModelScope.launch {
            authRepository.createAccount(email, password).onFailure { emitError(it) }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    fun deleteUser(email: String, password: String) {
        viewModelScope.launch {
            authRepository.deleteAccount(email, password).onFailure { emitError(it) }
        }
    }

    suspend fun updateHandle(userId: String, handle: String) {
        settingsRepository.updateHandle(userId, handle).onFailure { emitError(it) }
    }

    fun selectTheme(theme: Int) {
        viewModelScope.launch {
            userPreferencesRepository.saveThemePreference(theme)
        }
    }

    suspend fun setTaboo(userId: String?, taboo: Boolean) {
        if (userId != null) {
            settingsRepository.setTaboo(userId, taboo).onFailure { emitError(it) }
        } else {
            userPreferencesRepository.saveTabooPreference(taboo)
        }
    }

    suspend fun setCollection(userId: String?, collection: List<String>) {
        if (userId != null) {
            settingsRepository.setCollection(userId, collection).onFailure { emitError(it) }
        } else {
            userPreferencesRepository.saveCollectionPreference(collection)
        }
    }

    fun setEnglishSearchResultsSetting(isInclude: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.saveIncludeEnglishSearchResults(isInclude)
        }
    }

    /**
     * Called when the user enters a new search term.
     */
    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun clearSearchQuery() {
        _searchQuery.value = ""
    }

    fun getUsersByHandle(handle: String) {
        viewModelScope.launch {
            if (handle == "") _searchResults.value = emptyList()
            else settingsRepository.searchUsersByHandle(handle)
                .onSuccess { _searchResults.value = it }
                .onFailure {
                    emitError(it)
                    _searchResults.value = emptyList()
                }
        }
    }

    suspend fun sendFriendRequest(toUserId: String) {
        settingsRepository.friendRequestAction(FriendAction.SENT, toUserId)
            .onFailure { emitError(it) }
    }
    suspend fun acceptFriendRequest(toUserId: String) {
        settingsRepository.friendRequestAction(FriendAction.ACCEPT, toUserId)
            .onFailure { emitError(it) }
    }
    suspend fun rejectFriendRequest(toUserId: String) {
        settingsRepository.friendRequestAction(FriendAction.REVOKE, toUserId)
            .onFailure { emitError(it) }
    }

    suspend fun clearLocalData() {
        clearAllLocalDecksAndCampaignsUseCase.get().invoke()
            .onFailure { emitError(it) }
    }
}