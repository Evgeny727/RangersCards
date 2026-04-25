package com.rangerscards.di

import com.rangerscards.domain.repository.CampaignsRepository
import com.rangerscards.domain.repository.CardsRepository
import com.rangerscards.domain.repository.DecksRepository
import com.rangerscards.domain.usecase.ClearAllLocalDecksAndCampaignsUseCase
import com.rangerscards.domain.usecase.SearchCardsUseCase
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

    @Provides
    fun provideSearchCardsUseCase(
        cardsRepository: CardsRepository
    ) : SearchCardsUseCase = SearchCardsUseCase(cardsRepository)


}