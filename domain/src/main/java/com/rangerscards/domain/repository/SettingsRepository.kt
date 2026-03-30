package com.rangerscards.domain.repository

interface SettingsRepository {

    suspend fun deleteAllLocalDecks()

    suspend fun deleteAllLocalCampaigns()

}