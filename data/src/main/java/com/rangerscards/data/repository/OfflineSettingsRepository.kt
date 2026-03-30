package com.rangerscards.data.repository

import com.rangerscards.data.local.dao.CampaignDao
import com.rangerscards.data.local.dao.DeckDao
import com.rangerscards.domain.repository.SettingsRepository

class OfflineSettingsRepository(
    private val deckDao: DeckDao,
    private val campaignDao: CampaignDao
) : SettingsRepository {

    override suspend fun deleteAllLocalDecks() = deckDao.deleteAllLocalDecks()

    override suspend fun deleteAllLocalCampaigns() = campaignDao.deleteAllLocalCampaigns()

}