package com.rangerscards.domain.usecase

import com.rangerscards.domain.repository.CardsRepository

class GetAllPaginatedRoleCardsFlowUseCase(
    private val cardsRepository: CardsRepository
) {
    operator fun invoke(specialty: String, taboo: Boolean, packIds: List<String>) =
        cardsRepository.getAllPaginatedRoleCardsFlow(specialty, taboo, packIds)
}