package com.rangerscards.ui.deck

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.rangerscards.domain.model.CardDeckMulliganItem
import com.rangerscards.domain.model.CardWithCount
import com.rangerscards.domain.repository.CardsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class DeckMulliganViewModel @Inject constructor(
    private val cardsRepository: CardsRepository,
) : ViewModel() {

    private var _cardInPlay = MutableStateFlow<CardDeckMulliganItem?>(null)
    val cardInPlay = _cardInPlay.asStateFlow()

    val drawedCards = mutableStateListOf<CardDeckMulliganItem>()

    private var _cardsInDeck = MutableStateFlow<ImmutableList<CardDeckMulliganItem>>(persistentListOf())
    val cardsInDeck = _cardsInDeck.asStateFlow()

    val allCards = mutableStateListOf<CardDeckMulliganItem>()

    val selectedCards = mutableStateListOf<CardDeckMulliganItem>()

    suspend fun setSlots(slots: List<CardWithCount>) {
        val cardsMap = slots.associate { it.card.id to it.count }
        val cards = cardsRepository.getMulliganCardsByIds(cardsMap.keys.toList())
        val cardsWithCopies = cards.toMutableList()
        cards.forEach { card ->
            val count = cardsMap[card.id]!! - 1
            repeat(count) { index ->
                cardsWithCopies.add(card.copy(id = card.id + ".$index"))
            }
        }
        _cardsInDeck.value = cardsWithCopies.toImmutableList()
        allCards.clear()
        allCards.addAll(cardsWithCopies.shuffled())
    }

    fun setCardInPlay(card: CardDeckMulliganItem?) {
        _cardInPlay.value = card
        if (!allCards.remove(card)) {
            drawedCards.remove(card)
            selectedCards.remove(card)
        }
    }

    fun drawCards(count: Int) {
        repeat(count) {
            allCards.removeFirstOrNull()?.let {
                drawedCards.add(it)
            }
        }
    }

    fun resetCards() {
        drawedCards.clear()
        allCards.clear()
        allCards.addAll(_cardsInDeck.value.shuffled())
    }

    fun redrawSelected() {
        drawedCards.removeAll(selectedCards)
        drawCards(selectedCards.size)
        allCards.addAll(selectedCards)
        allCards.shuffle()
        selectedCards.clear()
    }

    fun reshuffleSelected() {
        drawedCards.removeAll(selectedCards)
        allCards.addAll(selectedCards)
        allCards.shuffle()
        selectedCards.clear()
    }
}