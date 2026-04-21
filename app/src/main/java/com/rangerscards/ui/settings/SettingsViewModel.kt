package com.rangerscards.ui.settings

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.net.toUri
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.imageLoader
import com.rangerscards.domain.model.User
import com.rangerscards.domain.model.UserInfo
import com.rangerscards.domain.repository.AuthRepository
import com.rangerscards.domain.repository.CampaignsRepository
import com.rangerscards.domain.repository.CardsRepository
import com.rangerscards.domain.repository.DecksRepository
import com.rangerscards.domain.repository.FriendAction
import com.rangerscards.domain.repository.SettingsRepository
import com.rangerscards.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UiErrorState(val exception: Throwable)


val SUPPORTED_LANGUAGES = listOf("en", "ru", "de", "fr", "it", "es")

/**
 * ViewModel to maintain user's settings.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val cardsRepository: CardsRepository,
    private  val settingsRepository: SettingsRepository,
    private val decksRepository: DecksRepository,
    private val campaignsRepository: CampaignsRepository,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val userFlow = authRepository.currentUserId
        .distinctUntilChanged()
        .flatMapLatest {
            if (it == null) flowOf(Result.success(null))
            else settingsRepository.startProfileSubscription(it)
        }

    private val _userUiState = MutableStateFlow(User())
    val userUiState = _userUiState.asStateFlow()

    private val _events = MutableSharedFlow<UiErrorState>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val events: SharedFlow<UiErrorState> = _events

    fun emitError(throwable: Throwable) {
        _events.tryEmit(UiErrorState(throwable))
    }

    init {
        viewModelScope.launch {
            userFlow.collect { result ->
                result.onSuccess { user ->
                    val data = result.getOrNull()
                    if (data == null) _userUiState.update { user ->
                        user.copy(
                            userInfo = null,
                            friends = persistentListOf(),
                            sentRequests = persistentListOf(),
                            receivedRequests = persistentListOf(),
                        )
                    } else {
                        _userUiState.update { user ->
                            user.copy(
                                userInfo = data.userInfo,
                                friends = data.friends,
                                sentRequests = data.sentRequests,
                                receivedRequests = data.receivedRequests,
                            )
                        }
                        val settings = data.settings
                        userPreferencesRepository.saveTabooAndCollectionPreference(settings.taboo, settings.collection)
                    }
                }.onFailure { exception ->
                    emitError(exception)
                }
            }
        }
    }

    init {
        // Collect values from the data store
        viewModelScope.launch {
            combine(userPreferencesRepository.isTabooSet, userPreferencesRepository.collection) {
                    taboo, collection -> Pair(taboo, collection)
            }.collect { (taboo, collection) ->
                _userUiState.update {
                    it.copy(
                        settings = it.settings.copy(
                            taboo = taboo,
                            collection = collection.toImmutableList()
                        )
                    )
                }
            }
        }
    }

    private val _cardsUpdatedAt =
        userPreferencesRepository.cardsUpdatedAt.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ""
        )

    // theme state
    val themeState: StateFlow<Int?> =
        userPreferencesRepository.isDarkTheme.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val isIncludeEnglishSearchResultsState: StateFlow<Boolean> =
        userPreferencesRepository.isIncludeEnglishSearchResults.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    private val _isCardsLoading = MutableStateFlow(false)
    val isCardsLoading = _isCardsLoading.asStateFlow()

    private val _isCardsUpdateAvailable = MutableStateFlow(false)
    val isCardsUpdateAvailable = _isCardsUpdateAvailable.asStateFlow()

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

    fun updateHandle(userId: String, handle: String) {
        if (handle == (userUiState.value.userInfo?.handle ?: "")) return
        viewModelScope.launch {
            settingsRepository.updateHandle(userId, handle).onFailure { emitError(it) }
        }
    }

    fun selectTheme(theme: Int) {
        viewModelScope.launch {
            userPreferencesRepository.saveThemePreference(theme)
        }
    }

    suspend fun setTaboo(taboo: Boolean) {
        val userId = _userUiState.value.userInfo?.id
        if (userId != null) {
            settingsRepository.setTaboo(userId, taboo).onFailure { emitError(it) }
        } else {
            userPreferencesRepository.saveTabooPreference(taboo)
        }
    }

    suspend fun setCollection(collection: List<String>) {
        val userId = _userUiState.value.userInfo?.id
        if (userId != null) {
            settingsRepository.setCollection(userId, collection).onFailure { emitError(it) }
        } else {
            userPreferencesRepository.saveCollectionPreference(collection)
        }
    }

    fun updateLocale(locale: String) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(locale))
        _userUiState.update { userUIState ->
            userUIState.copy(language = locale)
        }
        downloadCards()
    }

    private fun downloadCards() {
        _isCardsLoading.update { true }
        viewModelScope.launch {
            val language = coerceLanguage(_userUiState.value.language)
            cardsRepository.downloadAllCards(language)
                .onSuccess { userPreferencesRepository.saveCardsUpdatedTimestamp(it) }
                .onFailure { emitError(it) }
        }.invokeOnCompletion {
            _isCardsUpdateAvailable.update { false }
            _isCardsLoading.update { false }
        }
    }

    fun updateCardsIfNotUpdated() {
        _isCardsLoading.update { true }
        viewModelScope.launch {
            val language = coerceLanguage(_userUiState.value.language)
            cardsRepository.isCardsUpdateAvailable(
                language,
                _cardsUpdatedAt.value
            )
                .onSuccess { data ->
                    if (data) {
                        cardsRepository.downloadAllCards(language)
                            .onSuccess {
                                userPreferencesRepository.saveCardsUpdatedTimestamp(it)
                            }
                            .onFailure { emitError(it) }
                    } else {
                        _isCardsUpdateAvailable.update { false }
                        _isCardsLoading.update { false }
                    }
                }
                .onFailure { emitError(it) }
        }
    }

    fun cancelUpdateDialog() {
        _isCardsUpdateAvailable.update { false }
    }

    suspend fun checkCardsUpdateAvailable() {
        val language = coerceLanguage(_userUiState.value.language)
        cardsRepository.isCardsUpdateAvailable(
            language,
            _cardsUpdatedAt.value
        )
            .onSuccess {
                _isCardsUpdateAvailable.update { it }
            }
            .onFailure { emitError(it) }
    }

    private fun coerceLanguage(locale: String): String {
        return if (SUPPORTED_LANGUAGES.contains(locale)) locale
        else "en"
    }


    suspend fun downloadCardsIfDatabaseNotExists() {
        val exists = cardsRepository.isCardsTableExists()
        if (!exists) {
            downloadCards()
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
        _searchQuery.update {
            newQuery
        }
    }

    fun clearSearchQuery() {
        _searchQuery.update { "" }
    }

    fun getUsersByHandle(handle: String) {
        viewModelScope.launch {
            if (handle == "") _searchResults.update {
                emptyList()
            } else {
                settingsRepository.searchUsersByHandle(handle)
                    .onSuccess { _searchResults.update { it } }
                    .onFailure {
                        emitError(it)
                        _searchResults.update { emptyList() }
                    }
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
        decksRepository.deleteAllLocalDecks()
        campaignsRepository.deleteAllLocalCampaigns()
    }
}

fun Context.clearCoilCache() {
    val imageLoader = imageLoader
    // Clear memory cache.
    imageLoader.memoryCache?.clear()
    // Clear disk cache.
    imageLoader.diskCache?.clear()
}

fun Context.openLink(link: String) {
    startActivity(
        Intent(
            Intent.ACTION_VIEW,
            link.toUri()
        )
    )
}

fun Context.openEmail(email: String) {
    val uri = "mailto:$email".toUri()
    val intent = Intent(Intent.ACTION_SENDTO, uri)
    startActivity(intent)
}