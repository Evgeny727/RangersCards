package com.rangerscards.domain.usecase

import com.rangerscards.domain.repository.DecksRepository

class SearchDecksUseCase(
    private val decksRepository: DecksRepository
) {
    operator fun invoke(query: String, userId: String, uploaded: Boolean? = null) =
        if (query.isBlank()) decksRepository.getAllPaginatedDecksFlow(userId, uploaded)
            else decksRepository.searchPaginatedDecksFlow(query, userId, uploaded)
}