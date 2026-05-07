package com.rangerscards.data.di

import com.rangerscards.data.remote.AuthTokenProvider
import com.rangerscards.data.remote.FirebaseAuthTokenProvider
import com.rangerscards.data.repository.CampaignsRepositoryImpl
import com.rangerscards.data.repository.CardsRepositoryImpl
import com.rangerscards.data.repository.DecksRepositoryImpl
import com.rangerscards.data.repository.FirebaseAuthRepository
import com.rangerscards.data.repository.SettingsRepositoryImpl
import com.rangerscards.data.repository.UserPreferencesRepositoryImpl
import com.rangerscards.domain.repository.AuthRepository
import com.rangerscards.domain.repository.CampaignsRepository
import com.rangerscards.domain.repository.CardsRepository
import com.rangerscards.domain.repository.DecksRepository
import com.rangerscards.domain.repository.SettingsRepository
import com.rangerscards.domain.repository.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
interface BindModule {

    @Binds
    @Singleton
    fun bindUserPreferencesRepository(impl: UserPreferencesRepositoryImpl): UserPreferencesRepository

    @Binds
    @Singleton
    fun bindAuthRepository(impl: FirebaseAuthRepository): AuthRepository

    @Binds
    @Singleton
    fun bindAuthTokenProvider(impl: FirebaseAuthTokenProvider): AuthTokenProvider

    @Binds
    @Singleton
    fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    fun bindCardsRepository(impl: CardsRepositoryImpl): CardsRepository

    @Binds
    @Singleton
    fun bindDecksRepository(impl: DecksRepositoryImpl): DecksRepository

    @Binds
    @Singleton
    fun bindCampaignsRepository(impl: CampaignsRepositoryImpl): CampaignsRepository

}