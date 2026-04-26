package com.rangerscards.di

import com.rangerscards.domain.repository.CampaignsRepository
import com.rangerscards.domain.repository.CardsRepository
import com.rangerscards.domain.repository.DecksRepository
import com.rangerscards.domain.usecase.ClearAllLocalDecksAndCampaignsUseCase
import com.rangerscards.domain.usecase.CreateDeckUseCase
import com.rangerscards.domain.usecase.GetAllPaginatedRoleCardsFlowUseCase
import com.rangerscards.domain.usecase.GetRoleCardByCodeFlowUseCase
import com.rangerscards.domain.usecase.SearchCardsUseCase
import com.rangerscards.domain.usecase.SearchDecksUseCase
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

    @Provides
    fun provideSearchDecksUseCase(
        decksRepository: DecksRepository
    ) : SearchDecksUseCase = SearchDecksUseCase(decksRepository)

    @Provides
    fun provideGetAllPaginatedRoleCardsFlowUseCase(
        cardsRepository: CardsRepository
    ) : GetAllPaginatedRoleCardsFlowUseCase = GetAllPaginatedRoleCardsFlowUseCase(cardsRepository)

    @Provides
    fun provideGetRoleCardByCodeFlowUseCase(
        cardsRepository: CardsRepository
    ) : GetRoleCardByCodeFlowUseCase = GetRoleCardByCodeFlowUseCase(cardsRepository)

    @Provides
    fun provideCreateDeckUseCase(
        decksRepository: DecksRepository
    ) : CreateDeckUseCase = CreateDeckUseCase(decksRepository)

}