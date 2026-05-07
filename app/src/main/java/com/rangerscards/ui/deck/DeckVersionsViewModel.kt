package com.rangerscards.ui.deck

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rangerscards.domain.repository.DecksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeckVersionsViewModel @Inject constructor(
    private val decksRepository: DecksRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val deckId: String = checkNotNull(savedStateHandle["deckId"])

    // Holds the Ids of the deck versions.
    private val _deckVersionIds = MutableStateFlow<ImmutableList<String>>(persistentListOf())
    val deckVersionIds: StateFlow<ImmutableList<String>> = _deckVersionIds.asStateFlow()

    init {
        viewModelScope.launch {
            val ids = decksRepository.getAllDeckVersionIds(deckId)
            _deckVersionIds.value = ids.ifEmpty { persistentListOf(deckId) }
        }
    }
}