package com.rangerscards.domain.usecase

import androidx.paging.PagingData
import com.rangerscards.domain.model.CardDeckListItem
import com.rangerscards.domain.model.CardFilterOptions
import com.rangerscards.domain.model.DeckInfo
import com.rangerscards.domain.repository.CardsRepository
import kotlinx.coroutines.flow.Flow

class SearchDeckCardsUseCase(
    private val cardsRepository: CardsRepository
) {
    operator fun invoke(
        filterOptions: CardFilterOptions,
        deckInfo: DeckInfo,
        typeIndex: Int,
        showAllSpoilers: Boolean,
        packIds: List<String>,
        includeEnglish: Boolean,
    ): Flow<PagingData<CardDeckListItem>> {
        return cardsRepository.searchPaginatedDeckCardsFlow(
            filterOptions = filterOptions,
            deckInfo = deckInfo,
            typeIndex = typeIndex,
            showAllSpoilers = showAllSpoilers,
            packIds = packIds,
            includeEnglish = if (filterOptions.searchQuery.isEmpty()) null else includeEnglish
        )
    }
}