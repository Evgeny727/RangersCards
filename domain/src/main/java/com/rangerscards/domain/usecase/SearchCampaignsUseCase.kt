package com.rangerscards.domain.usecase

import com.rangerscards.domain.repository.CampaignsRepository

class SearchCampaignsUseCase(
    private val campaignsRepository: CampaignsRepository
) {
    operator fun invoke(query: String, userId: String) =
        if (query.isBlank()) campaignsRepository.getAllPaginatedCampaignsFlow()
        else campaignsRepository.searchPaginatedCampaignsFlow(query)
}