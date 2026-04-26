package com.rangerscards.domain.usecase

import androidx.paging.PagingData
import com.rangerscards.domain.model.CardFilterOptions
import com.rangerscards.domain.model.CardListItem
import com.rangerscards.domain.repository.CardsRepository
import kotlinx.coroutines.flow.Flow

class SearchCardsUseCase (
    private val cardsRepository: CardsRepository
) {
    operator fun invoke(
        filterOptions: CardFilterOptions,
        includeEnglish: Boolean,
        spoiler: Boolean,
        taboo: Boolean,
        packIds: List<String>
    ): Flow<PagingData<CardListItem>> {
        return cardsRepository.searchPaginatedCardsFlow(
            filterOptions = filterOptions,
            spoiler = spoiler,
            taboo = taboo,
            packIds = packIds,
            includeEnglish = if (filterOptions.searchQuery.isEmpty()) null else includeEnglish
        )
    }
}