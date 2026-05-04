package com.rangerscards.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.imageLoader
import com.rangerscards.UiErrorState
import com.rangerscards.domain.repository.AuthRepository
import com.rangerscards.domain.repository.SettingsRepository
import com.rangerscards.domain.repository.UserPreferencesRepository
import com.rangerscards.domain.usecase.ClearAllLocalDecksAndCampaignsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SettingsUiState {
    object Idle : SettingsUiState
    object Loading : SettingsUiState

    data class Success(val message: String) : SettingsUiState
}

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

    private val _settingsUiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Idle)
    val settingsUiState: StateFlow<SettingsUiState> = _settingsUiState.asStateFlow()

    private val _events = MutableSharedFlow<UiErrorState>(extraBufferCapacity = 1)
    val events: SharedFlow<UiErrorState> = _events

    fun emitError(throwable: Throwable) {
        _events.tryEmit(UiErrorState(throwable))
    }

    private val _userEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val userEvents: SharedFlow<Unit> = _userEvents.asSharedFlow()

    private fun emitUserEvent() {
        _userEvents.tryEmit(Unit)
    }

    val isIncludeEnglishSearchResultsState: StateFlow<Boolean> =
        userPreferencesRepository.isIncludeEnglishSearchResults

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _settingsUiState.value = SettingsUiState.Loading
            authRepository.signIn(email, password).onFailure { emitError(it) }
            _settingsUiState.value = SettingsUiState.Idle
        }
    }

    fun createAccount(email: String, password: String) {
        viewModelScope.launch {
            _settingsUiState.value = SettingsUiState.Loading
            authRepository.createAccount(email, password).onFailure { emitError(it) }
            _settingsUiState.value = SettingsUiState.Idle
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _settingsUiState.value = SettingsUiState.Loading
            authRepository.signOut()
            _settingsUiState.value = SettingsUiState.Idle
        }
    }

    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            _settingsUiState.value = SettingsUiState.Loading
            authRepository.sendPasswordResetEmail(email).onFailure { emitError(it) }
            _settingsUiState.value = SettingsUiState.Idle
        }
    }

    fun deleteUser(email: String, password: String) {
        viewModelScope.launch {
            _settingsUiState.value = SettingsUiState.Loading
            authRepository.deleteAccount(email, password).onFailure { emitError(it) }
            _settingsUiState.value = SettingsUiState.Idle
        }
    }

    fun updateHandle(userId: String, handle: String) {
        viewModelScope.launch {
            _settingsUiState.value = SettingsUiState.Loading
            settingsRepository.updateHandle(userId, handle)
                .onSuccess { emitUserEvent() }
                .onFailure { emitError(it) }
            _settingsUiState.value = SettingsUiState.Idle
        }
    }

    fun selectTheme(theme: Int) {
        viewModelScope.launch {
            userPreferencesRepository.saveThemePreference(theme)
        }
    }

    fun setTaboo(userId: String?, taboo: Boolean) {
        viewModelScope.launch {
            _settingsUiState.value = SettingsUiState.Loading
            if (userId != null) {
                settingsRepository.setTaboo(userId, taboo)
                    .onSuccess { emitUserEvent() }
                    .onFailure { emitError(it) }
            } else {
                userPreferencesRepository.saveTabooPreference(taboo)
            }
            _settingsUiState.value = SettingsUiState.Idle
        }
    }

    fun setCollection(userId: String?, collection: List<String>) {
        viewModelScope.launch {
            _settingsUiState.value = SettingsUiState.Loading
            if (userId != null) {
                settingsRepository.setCollection(userId, collection)
                    .onSuccess { emitUserEvent(); }
                    .onFailure { emitError(it) }
            } else {
                userPreferencesRepository.saveCollectionPreference(collection)
            }
            _settingsUiState.value = SettingsUiState.Idle
        }
    }

    fun setEnglishSearchResultsSetting(isInclude: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.saveIncludeEnglishSearchResults(isInclude)
        }
    }

    fun clearLocalData(message: String) {
        _settingsUiState.value = SettingsUiState.Loading
        viewModelScope.launch {
            clearAllLocalDecksAndCampaignsUseCase.get().invoke()
                .onFailure { emitError(it) }
            _settingsUiState.value = SettingsUiState.Success(message)
        }
    }

    fun clearImageCache(context: Context, message: String) {
        _settingsUiState.value = SettingsUiState.Loading
        val imageLoader = context.imageLoader
        // Clear memory cache.
        imageLoader.memoryCache?.clear()
        // Clear disk cache.
        imageLoader.diskCache?.clear()
        _settingsUiState.value = SettingsUiState.Success(message)
    }
}