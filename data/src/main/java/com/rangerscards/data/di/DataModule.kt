package com.rangerscards.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.rangerscards.data.local.RangersDatabase
import com.rangerscards.data.local.dao.CampaignDao
import com.rangerscards.data.local.dao.CardDao
import com.rangerscards.data.local.dao.DeckDao
import com.rangerscards.data.local.migrations.MigrationCampaignChallengeDeck
import com.rangerscards.data.local.migrations.MigrationCampaignExpansions
import com.rangerscards.data.local.migrations.MigrationCampaignTransfer
import com.rangerscards.data.local.migrations.MigrationCardSetupField
import com.rangerscards.data.local.migrations.MigrationRemoveFlavorFromFTS
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun providePreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create {
            context.preferencesDataStoreFile("settings_preferences")
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RangersDatabase {
        return Room.databaseBuilder(
            context,
            RangersDatabase::class.java,
            "rangers_database"
        )
            .addMigrations(
                MigrationCampaignTransfer,
                MigrationCampaignChallengeDeck,
                MigrationRemoveFlavorFromFTS,
                MigrationCampaignExpansions,
                MigrationCardSetupField,
            )
            .build()
    }

    @Provides
    @Singleton
    fun provideCardDao(db: RangersDatabase): CardDao = db.cardDao()

    @Provides
    @Singleton
    fun provideDeckDao(db: RangersDatabase): DeckDao = db.deckDao()

    @Provides
    @Singleton
    fun provideCampaignDao(db: RangersDatabase): CampaignDao = db.campaignDao()

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)

}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope