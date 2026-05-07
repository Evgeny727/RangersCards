package com.rangerscards.domain.usecase

import com.rangerscards.domain.repository.CardsRepository
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.map

class GetRolesImagesByIdFlowUseCase(
    private val cardsRepository: CardsRepository
) {
    operator fun invoke(ids: List<String>) =
        cardsRepository.getRoleCardsByIdFlow(ids).map { rolesList ->
            // Create a map from id to RoleCard
            val itemById = rolesList.associateBy { it.id }
            // Map the list of ids to the corresponding real image URLs.
            ids.map { id -> itemById[id]?.realImageSrc.orEmpty() }.toImmutableList()
        }
}