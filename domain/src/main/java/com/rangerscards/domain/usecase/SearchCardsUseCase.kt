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
        return if (filterOptions.searchQuery.isEmpty()) {
            cardsRepository.getAllPaginatedCardsFlow(
                spoiler,
                taboo,
                packIds,
                filterOptions
            )
        } else {
            cardsRepository.searchPaginatedCardsFlow(
                filterOptions = filterOptions,
                includeEnglish = includeEnglish,
                spoiler = spoiler,
                taboo = taboo,
                packIds = packIds
            )
        }
    }
}