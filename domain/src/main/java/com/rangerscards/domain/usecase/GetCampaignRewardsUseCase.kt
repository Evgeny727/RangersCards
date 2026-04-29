package com.rangerscards.domain.usecase

import com.rangerscards.domain.model.CardListItem
import com.rangerscards.domain.model.UserSettings
import com.rangerscards.domain.repository.CardsRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow

class GetCampaignRewardsUseCase(
    private val cardsRepository: CardsRepository
) {
    operator fun invoke(
        query: String,
        userSettings: UserSettings,
        packId: String,
        showAll: Boolean
    ): Flow<ImmutableList<CardListItem>> {
        val filteredCollection = userSettings.collection.toSet().filter {
            if (packId == "core") it != "loa" else true
        }
        val packIds = if (showAll) filteredCollection + packId else listOf(packId)
        return cardsRepository.getRewards(query, userSettings.taboo, packIds)
    }
}