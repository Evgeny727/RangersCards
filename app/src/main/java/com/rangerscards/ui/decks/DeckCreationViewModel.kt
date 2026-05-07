package com.rangerscards.ui.decks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.rangerscards.UiErrorState
import com.rangerscards.domain.model.DeckMeta
import com.rangerscards.domain.model.RoleCard
import com.rangerscards.domain.usecase.CreateDeckUseCase
import com.rangerscards.domain.usecase.GetAllPaginatedRoleCardsFlowUseCase
import com.rangerscards.domain.usecase.GetRoleCardByCodeFlowUseCase
import com.rangerscards.objects.StarterDecks
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface DeckCreationUiState {
    object Idle : DeckCreationUiState
    object Loading : DeckCreationUiState
    data class Success(val deckId: String) : DeckCreationUiState
    object Error : DeckCreationUiState
}

@HiltViewModel
class DeckCreationViewModel @Inject constructor(
    private val getAllPaginatedRoleCardsFlowUseCase: GetAllPaginatedRoleCardsFlowUseCase,
    private val getRoleCardByCodeFlowUseCase: GetRoleCardByCodeFlowUseCase,
    private val createDeckUseCase: CreateDeckUseCase,
) : ViewModel() {

    private val _deckCreationUiState = MutableStateFlow<DeckCreationUiState>(DeckCreationUiState.Idle)
    val deckCreationUiState: StateFlow<DeckCreationUiState> = _deckCreationUiState.asStateFlow()

    private val _events = MutableSharedFlow<UiErrorState>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val events: SharedFlow<UiErrorState> = _events

    private fun emitError(throwable: Throwable) {
        _events.tryEmit(UiErrorState(throwable))
    }

    fun getRoles(specialty: String, taboo: Boolean, packIds: List<String>): Flow<PagingData<RoleCard>> =
        getAllPaginatedRoleCardsFlowUseCase(specialty, taboo, packIds).cachedIn(viewModelScope)

    fun getRoleCard(code: String, taboo: Boolean): Flow<RoleCard> =
        getRoleCardByCodeFlowUseCase(code, taboo)

    fun createDeck(
        name: String,
        deckMeta: DeckMeta?,
        backgroundLocalized: String,
        specialtyLocalized: String,
        isUploading: Boolean,
        starterDeckId: Int,
        postfix: String,
        taboo: Boolean,
    ) {
        viewModelScope.launch {
            _deckCreationUiState.value = DeckCreationUiState.Loading
            createDeckUseCase(
                starterDeck = StarterDecks.starterDecks().getOrNull(starterDeckId),
                deckMeta = deckMeta,
                backgroundLocalized = backgroundLocalized,
                specialtyLocalized = specialtyLocalized,
                postfix = postfix,
                isUploading = isUploading,
                name = name,
                tabooSetId = if (taboo) CURRENT_TABOO_SET else null,
            ).onFailure {
                emitError(it)
                _deckCreationUiState.value = DeckCreationUiState.Error
            }.onSuccess {
                _deckCreationUiState.value = DeckCreationUiState.Success(it)
            }
        }
    }

}