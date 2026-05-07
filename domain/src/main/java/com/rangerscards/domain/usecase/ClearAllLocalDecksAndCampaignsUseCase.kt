package com.rangerscards.domain.usecase

import com.rangerscards.domain.repository.CampaignsRepository
import com.rangerscards.domain.repository.DecksRepository

class ClearAllLocalDecksAndCampaignsUseCase (
    private val decksRepository: DecksRepository,
    private val campaignsRepository: CampaignsRepository,
) {
    suspend operator fun invoke() = runCatching {
        decksRepository.deleteAllLocalDecks()
        campaignsRepository.deleteAllLocalCampaigns()
    }
}