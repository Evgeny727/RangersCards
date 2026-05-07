package com.rangerscards.domain.usecase

import com.rangerscards.domain.repository.CardsRepository

class GetRoleCardByCodeFlowUseCase(
    private val cardsRepository: CardsRepository
) {
    operator fun invoke(code: String, taboo: Boolean) =
        cardsRepository.getRoleCardByCodeFlow(code, taboo)
}