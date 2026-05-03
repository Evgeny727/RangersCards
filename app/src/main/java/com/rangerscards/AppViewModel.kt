package com.rangerscards

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rangerscards.domain.model.User
import com.rangerscards.domain.repository.AuthRepository
import com.rangerscards.domain.repository.SettingsRepository
import com.rangerscards.domain.repository.UserPreferencesRepository
import com.rangerscards.ui.CardsSyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UiErrorState(val exception: Throwable)

val SUPPORTED_LANGUAGES = listOf("en", "ru", "de", "fr", "it", "es")

@HiltViewModel
class AppViewModel @Inject constructor(
    authRepository: AuthRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val settingsRepository: SettingsRepository,
    private val cardsSyncManager: CardsSyncManager,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val userFlow = authRepository.currentUserId
        .flatMapLatest {
            if (it == null) flowOf(Result.success(null) to Result.success(null))
            else settingsRepository.startUserSubscription(it)
        }

    private val _userUiState = MutableStateFlow(User())
    val userUiState = _userUiState.asStateFlow()

    private val _events = MutableSharedFlow<UiErrorState>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun emitError(throwable: Throwable) {
        _events.tryEmit(UiErrorState(throwable))
    }

    val cardsSyncState = cardsSyncManager.state

    val themeState: StateFlow<Int?> =
        userPreferencesRepository.isDarkTheme.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    init {
        observeUser()
        observePreferences()
        observeCardsErrors()
    }

    private fun observeUser() {
        viewModelScope.launch {
            userFlow.collect { result ->
                result.first.onSuccess { user ->
                    if (user == null) _userUiState.update { state ->
                        state.copy(
                            userInfo = null,
                            friends = persistentListOf(),
                            sentRequests = persistentListOf(),
                            receivedRequests = persistentListOf(),
                        )
                    } else _userUiState.update { state ->
                        state.copy(
                            userInfo = user.userInfo,
                            friends = user.friends,
                            sentRequests = user.sentRequests,
                            receivedRequests = user.receivedRequests,
                        )
                    }
                }.onFailure { exception -> emitError(exception) }
                result.second.onSuccess { settings ->
                    settings?.let {
                        userPreferencesRepository.saveTabooAndCollectionPreference(
                            settings.taboo,
                            settings.collection
                        )
                    }
                }.onFailure { exception -> emitError(exception) }
            }
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            combine(
                userPreferencesRepository.isTabooSet,
                userPreferencesRepository.collection
            ) { taboo, collection ->
                taboo to collection
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

    private fun observeCardsErrors() {
        viewModelScope.launch {
            cardsSyncManager.errors.collect {
                emitError(it)
            }
        }
    }

    suspend fun checkIfCardsReady() {
        val language = coerceLanguage(userUiState.value.language)
        cardsSyncManager.ensureCardsReady(language)
    }

    fun confirmCardsUpdate() {
        viewModelScope.launch {
            val language = coerceLanguage(userUiState.value.language)
            cardsSyncManager.download(language)
        }
    }

    fun cancelCardsUpdate() {
        cardsSyncManager.cancelUpdateDialog()
    }

    fun updateCardsIfAvailable() {
        viewModelScope.launch {
            val language = coerceLanguage(userUiState.value.language)
            cardsSyncManager.updateCardsIfUpdateAvailable(language)
        }
    }

    fun updateLocale(locale: String) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(locale))
        _userUiState.update { userUIState ->
            userUIState.copy(language = locale)
        }
        viewModelScope.launch {
            cardsSyncManager.download(coerceLanguage(locale))
        }
    }

    private fun coerceLanguage(locale: String): String {
        return if (SUPPORTED_LANGUAGES.contains(locale)) locale else "en"
    }

}