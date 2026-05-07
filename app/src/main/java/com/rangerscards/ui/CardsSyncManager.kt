package com.rangerscards.ui

import com.rangerscards.domain.repository.CardsRepository
import com.rangerscards.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

sealed interface CardsSyncState {
    object Idle : CardsSyncState
    object Loading : CardsSyncState
    object UpdateAvailable : CardsSyncState
    object Ready : CardsSyncState
}

class CardsSyncManager @Inject constructor(
    private val cardsRepository: CardsRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) {

    private val _state = MutableStateFlow<CardsSyncState>(CardsSyncState.Idle)
    val state: StateFlow<CardsSyncState> = _state.asStateFlow()

    private val _errors = MutableSharedFlow<Throwable>(extraBufferCapacity = 1)
    val errors: SharedFlow<Throwable> = _errors

    private val cardsUpdatedAt = userPreferencesRepository.cardsUpdatedAt

    suspend fun ensureCardsReady(language: String) {
        if (!cardsRepository.isCardsTableExists()) {
            download(language)
        } else {
            checkForUpdate(language)
        }
    }

    suspend fun checkForUpdate(language: String) {
        fetchCardsUpdate(language) {
            if (it) {
                _state.value = CardsSyncState.UpdateAvailable
            } else {
                _state.value = CardsSyncState.Ready
            }
        }
    }

    suspend fun updateCardsIfUpdateAvailable(language: String) {
        _state.value = CardsSyncState.Loading
        fetchCardsUpdate(language) { isAvailable ->
            if (isAvailable) {
                cardsRepository.downloadAllCards(language)
                    .onSuccess {
                        userPreferencesRepository.saveCardsUpdatedTimestamp(it)
                        _state.value = CardsSyncState.Ready
                    }
                    .onFailure {
                        _errors.tryEmit(it)
                        _state.value = CardsSyncState.Ready
                    }
            } else {
                _state.value = CardsSyncState.Ready
            }
        }
    }

    private suspend fun fetchCardsUpdate(
        language: String,
        block: suspend (Boolean) -> Unit
    ): Result<Boolean> {
        return cardsRepository.isCardsUpdateAvailable(
            language,
            cardsUpdatedAt.first()
        )
            .onSuccess { block(it) }
            .onFailure {
                _errors.tryEmit(it)
                _state.value = CardsSyncState.Ready
            }
    }

    suspend fun download(language: String) {
        _state.value = CardsSyncState.Loading

        cardsRepository.downloadAllCards(language)
            .onSuccess {
                userPreferencesRepository.saveCardsUpdatedTimestamp(it)
                _state.value = CardsSyncState.Ready
            }
            .onFailure {
                _errors.tryEmit(it)
                _state.value = CardsSyncState.Ready
            }
    }

    fun cancelUpdateDialog() {
        _state.value = CardsSyncState.Ready
    }
}