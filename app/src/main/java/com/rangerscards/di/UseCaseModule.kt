package com.rangerscards.di

import com.rangerscards.domain.repository.CampaignsRepository
import com.rangerscards.domain.repository.DecksRepository
import com.rangerscards.domain.usecase.ClearAllLocalDecksAndCampaignsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface UseCaseModule {

    @Provides
    fun provideClearAllLocalDecksAndCampaignsUseCase(
        decksRepository: DecksRepository,
        campaignsRepository: CampaignsRepository,
    ) : ClearAllLocalDecksAndCampaignsUseCase {
        return ClearAllLocalDecksAndCampaignsUseCase(decksRepository, campaignsRepository)
    }

}